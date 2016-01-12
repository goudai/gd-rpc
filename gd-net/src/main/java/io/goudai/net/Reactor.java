package io.goudai.net;

import io.goudai.common.Life;
import io.goudai.session.AbstractSession;
import io.goudai.session.Session;
import io.goudai.session.factory.SessionFactory;
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
    /*负责构造具体的session*/
    private final SessionFactory sessionFactory;


    public Reactor(String name,SessionFactory sessionFactory) throws IOException {
        super(name);
        this.selector = Selector.open();
        this.sessionFactory = sessionFactory;
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
                    arsc.session = sessionFactory.make(arsc.socketChannel, key);
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

    /**
     * run是开启了另外一个线程
     当你的操作当前线程 向run线程进行事件注册 这是不被允许的
     所以要用一个队列来进行异步注册
     那个判断的意思是如果当前是run线程 那么直接注册
     否者 就进行异步注册
     * @param socketChannel
     * @param ops
     * @param session
     * @throws ClosedChannelException
     */
    public void register(SocketChannel socketChannel, int ops,AbstractSession session) throws ClosedChannelException {
        if(this == Thread.currentThread()){
            SelectionKey key = socketChannel.register(this.selector, ops);
            if(session == null){
                session = sessionFactory.make(socketChannel, key);
            }
            key.attach(session);
        }else{
            this.asyncRegistrySocketChannels.offer(new AsyncRegistrySocketChannel(socketChannel,ops,session));
            this.doWakeup();
        }
    }


   public static class AsyncRegistrySocketChannel {
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
