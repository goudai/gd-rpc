package io.goudai.net;

import io.goudai.net.common.Lifecycle;
import io.goudai.net.session.AbstractSession;
import io.goudai.net.session.Session;
import io.goudai.net.session.factory.SessionFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by freeman on 2016/1/8.
 * 用于处理网络的read write事件
 */
@Getter
@Slf4j
public class Reactor extends Thread implements Lifecycle {

    /*处理读写事件的selector*/
    private final Selector selector;
    /*由于channel注册不能进行跨线程，所以使用一个队列来进行异步的注册*/
    private Queue<AsyncRegistrySocketChannel> asyncRegistrySocketChannels = new ConcurrentLinkedQueue<>();
    /*唤醒标记用于减少唤醒次数*/
    private final static AtomicBoolean wakeup = new AtomicBoolean(false);
    /*负责构造具体的session*/
    private final SessionFactory sessionFactory;


    public Reactor(String name, SessionFactory sessionFactory) throws IOException {
        super(name);
        this.setDaemon(true);
        this.selector = Selector.open();
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void startup()  {
        this.start();
        log.info("reactor {} started success", this.getName());
    }

    @Override
    public void shutdown()  {
        try {
            this.selector.close();
            this.interrupt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void register(SocketChannel socketChannel, int ops, AbstractSession session) throws ClosedChannelException {
        this.asyncRegistrySocketChannels.offer(new AsyncRegistrySocketChannel(socketChannel, ops, session));
        this.doWakeup();
    }

    @Override
    public void run() {
        while (!interrupted()) {
            doSelect();
            if(this.selector.isOpen()){
                Set<SelectionKey> selectionKeys = this.selector.selectedKeys();
                try {
                    selectionKeys.forEach(this::react);
                } finally {
                    selectionKeys.clear();
                }
            }else return;

        }

    }

    private void react(SelectionKey key) {
        try {
            if (key.isValid()) {
                Session session = (Session) key.attachment();
                session.setUpdateTime(new Date());
                if (key.isReadable()) {
                    session.read();
                } else if (key.isWritable()) {
                    session.realWrite();
                } else {
                    key.cancel();
                }
            }
        } catch (Exception e) {
            try {
                key.channel().close();
            } catch (IOException e1) {
//                ig
            }
            key.cancel();

            log.warn(e.getMessage(),e);

        }
    }

    private void doSelect() {
        try {
            wakeup.compareAndSet(true, false);
            this.selector.select();
            this.asyncRegistry();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void asyncRegistry() {
        AsyncRegistrySocketChannel arsc = null;
        while ((arsc = this.asyncRegistrySocketChannels.poll()) != null) {
            try {
                if (arsc.session != null) {
                    SelectionKey key = arsc.socketChannel.register(selector, arsc.ops, arsc.session);
                    arsc.session.setKey(key);
                } else {
                    SelectionKey key = arsc.socketChannel.register(selector, arsc.ops);
                    arsc.session = sessionFactory.make(arsc.socketChannel, key);
                    key.attach(arsc.session);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void doWakeup() {
        final Selector selector = this.selector;
        if (wakeup.compareAndSet(false, true)) {
            selector.wakeup();
        }
    }


    private static class AsyncRegistrySocketChannel {
        private SocketChannel socketChannel;
        private int ops;
        private AbstractSession session;

        public AsyncRegistrySocketChannel(SocketChannel socketChannel, int ops, AbstractSession session) {
            this.socketChannel = socketChannel;
            this.ops = ops;
            this.session = session;
        }
    }


}
