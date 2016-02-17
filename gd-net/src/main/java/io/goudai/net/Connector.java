package io.goudai.net;

import io.goudai.net.common.Lifecycle;
import io.goudai.net.context.ContextHolder;
import io.goudai.net.session.AbstractSession;
import io.goudai.net.session.Session;
import io.goudai.net.session.factory.SessionFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by freeman on 2016/1/8.
 */
@Getter
@Slf4j
public class Connector extends Thread implements Lifecycle {

    private final Selector selector;
    private final ReactorPool reactorPool;
    /*标记是否启动*/
    private final AtomicBoolean started = new AtomicBoolean(false);
    private Queue<Session> asyncSessionQueue = new ConcurrentLinkedQueue<>();

    public Connector(String name, ReactorPool reactorPool) throws IOException {
        super(name);
        this.setDaemon(true);
        this.reactorPool = reactorPool;
        this.selector = Selector.open();
    }

    @Override
    public void startup() {
        this.started.set(true);
        this.start();
        log.info("connect {} started success", this.getName());
    }

    @Override
    public void shutdown() {
        log.info("connect {} shutdowning", this.getName());
        try {
            this.selector.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Session connect(InetSocketAddress remoteAddress, long timeout, SessionFactory sessionFactory) throws IOException, InterruptedException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        Session session = (Session) sessionFactory.make(socketChannel, null);
        this.asyncSessionQueue.offer(session);
        socketChannel.connect(remoteAddress);
        this.selector.wakeup();
        session.awaitConnected(timeout);
        return session;
    }

    @Override
    public void run() {
        while (started.get()) {
            if (this.selector.isOpen()) {
                this.doSelect();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                try {
                    selectionKeys.forEach(this::connect);
                } finally {
                    selectionKeys.clear();
                }
            } else {
                log.error("{} selector is closed ,selector = [{}]", this.getName(), this.selector);
                return;
            }
        }
    }

    private void connect(SelectionKey key) {
        try {
            if (key.isValid() && key.isConnectable()) {
                AbstractSession session = (AbstractSession) key.attachment();
                while (!session.getSocketChannel().finishConnect()) {
                    log.info("check finish connection");
                }
                key.interestOps(0);
                session.finishConnect();
                ContextHolder.getContext().getSessionListener().onConnected(session);
                reactorPool.register(session.getSocketChannel(), session);
            } else
                key.cancel();

        } catch (Exception e) {
            key.cancel();
            log.error(e.getMessage(), e);
        }
    }

    private void handleAsyncSessionQueue() {
        Session session = null;
        while ((session = this.asyncSessionQueue.poll()) != null) {
            try {
                SelectionKey key = session.getSocketChannel().register(selector, SelectionKey.OP_CONNECT, session);
                session.setKey(key);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void doSelect() {
        try {
            this.selector.select();
            this.handleAsyncSessionQueue();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

    }


}
