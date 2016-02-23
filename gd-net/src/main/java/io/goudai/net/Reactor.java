package io.goudai.net;

import io.goudai.net.common.Lifecycle;
import io.goudai.net.context.ContextHolder;
import io.goudai.net.session.AbstractSession;
import io.goudai.net.session.factory.SessionFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
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
    /*负责构造具体的session*/
    private final SessionFactory sessionFactory;
    /*由于channel注册不能进行跨线程，所以使用一个队列来进行异步的注册*/
    private final Queue<AsyncRegistrySocketChannel> asyncRegistrySocketChannels = new ConcurrentLinkedQueue<>();
    /*标记是否启动*/
    private final AtomicBoolean started = new AtomicBoolean(false);


    public Reactor(String name, SessionFactory sessionFactory) throws IOException {
        super(name);
        this.setDaemon(true);
        this.selector = Selector.open();
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void startup() {
        this.started.set(true);
        this.start();
        log.info("reactor {} started success", this.getName());
    }

    @Override
    public void shutdown() {
        try {
            this.started.set(false);
            this.selector.close();
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
        while (started.get()) {
            this.doSelect();
            if (this.selector.isOpen()) {
                Set<SelectionKey> selectionKeys = this.selector.selectedKeys();
                try {
                    selectionKeys.forEach(this::react);
                } finally {
                    selectionKeys.clear();
                }
            } else {
                log.error("{} selector is closed ,selector = [{}]", this.getName(), this.selector);
                return;
            }
        }


    }

    private void react(SelectionKey key) {
        AbstractSession session = null;
        try {
            if (key.isValid()) {
                session = (AbstractSession) key.attachment();
                session.updateTime();
                if (key.isReadable()) {
                    session.read();
                } else if (key.isWritable()) {
                    session.realWrite();
                } else {
                    key.cancel();
                }
            }
        } catch (Exception e) {
            if (session != null) {
                session.setStatus(AbstractSession.Status.ERROR);
                ContextHolder.getContext().getSessionListener().onException(session, e);
            } else {
                try {
                    key.cancel();
                    key.channel().close();
                } catch (IOException e1) {
                    //ig
                }
            }
        }
    }

    private void doSelect() {
        try {
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
                ContextHolder.getContext().getSessionListener().onOpen(arsc.session);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void doWakeup() {
        final Selector selector = this.selector;
        selector.wakeup();
    }


    private final class AsyncRegistrySocketChannel {
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
