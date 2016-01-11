package io.goudai.net;

import io.goudai.common.Life;
import io.goudai.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class Reactor extends Thread implements Life{

    private static final Logger logger = LoggerFactory.getLogger(Reactor.class);
    /*处理读写事件的selector*/
    private final Selector selector;
    /*由于channel注册不能进行跨线程，所以使用一个队列来进行异步的注册*/
    private Queue<AsyncRegistrySocketChannel> asyncRegistrySocketChannels = new ConcurrentLinkedQueue<>();
    /*唤醒标记用于减少唤醒次数*/
    private AtomicBoolean wakeup = new AtomicBoolean(false);


    public Reactor(String name) throws IOException {
        super(name);
        this.selector = Selector.open();
    }

    @Override
    public void startup() throws Exception {
        this.start();
        logger.info("reactor {} started success",this.getName());
    }

    @Override
    public void shutdown() throws Exception {
        this.selector.close();
        this.interrupt();
    }

    @Override
    public void run() {
        final Selector selector = this.selector;
        while (!interrupted()) {
            doSelect();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            try {
                selectionKeys.forEach(this::react);
            }finally {
                selectionKeys.clear();
            }
        }

    }

    private void react(SelectionKey key) {
        try {
            if (key.isValid()) {
                Session session = (Session) key.attachment();
                session.setUpdateTime(new Date());
                //TODO 具体设计待定
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
                key.cancel();
            } catch (IOException e1) {
                logger.error(e.getMessage(), e);
            }
            logger.error(e.getMessage(), e);

        }
    }

    private void doSelect() {
        try {
            wakeup.compareAndSet(true, false);
            this.selector.select();
            this.asyncRegistry();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
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
                    arsc.session = new Session(arsc.socketChannel, key, new Date());
                    key.attach(arsc.session);
                }
//                NioConfig.getHandler().onSessionCreated(session);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void doWakeup() {
        final Selector selector = this.selector;
        if (wakeup.compareAndSet(false, true)) {
            selector.wakeup();
        }
    }

    public void register(SocketChannel socketChannel, int ops,Session session) throws ClosedChannelException {
        if(this == Thread.currentThread()){
            SelectionKey key = socketChannel.register(this.selector, ops);
            session = new Session(socketChannel, key, new Date());
            key.attach(session);
        }else{
            this.asyncRegistrySocketChannels.offer(new AsyncRegistrySocketChannel(socketChannel,ops,session));
            this.doWakeup();
        }
    }


   public static class AsyncRegistrySocketChannel {
        private SocketChannel socketChannel;
        private int ops;
        private Session session;

        public AsyncRegistrySocketChannel(SocketChannel socketChannel, int ops, Session session) {
            this.socketChannel = socketChannel;
            this.ops = ops;
            this.session = session;
        }
    }


}
