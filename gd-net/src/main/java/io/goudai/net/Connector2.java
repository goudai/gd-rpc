package io.goudai.net;

import io.goudai.session.Session;
import io.goudai.session.factory.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Administrator on 2015/8/17.
 */
public class Connector2 extends Thread implements Closeable {
    private static Logger logger = LoggerFactory.getLogger(Connector.class);
    private Selector selector;
    private ReactorPool reactorPool;
    private Queue<Session> regQ = new ConcurrentLinkedQueue<>();


    public Connector2(String name, ReactorPool reactorPool) throws IOException {
        super(name);
        this.reactorPool = reactorPool;
        this.selector = Selector.open();
    }

    @Override
    public void run() {
        while (!interrupted()) {
            try {
                this.selector.select();
                this.handleReg(selector);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                selectionKeys.forEach(this::handleConnectEventKey);
                selectionKeys.clear();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }


    }

    private void handleConnectEventKey(SelectionKey key) {
        try {
            if (key.isValid()) {
                if (key.isConnectable()) {
                    Session session = (Session) key.attachment();
                    while (!session.getSocketChannel().finishConnect()) {
                        System.out.println("check finish connection");
                    }
                    key.interestOps(0);
                    key.cancel();
                    reactorPool.register(session.getSocketChannel(), session);
                } else {
                    key.cancel();
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    public Session connect(InetSocketAddress remoteAddress, SessionFactory sessionFactory) throws IOException, InterruptedException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);

        Session session = (Session) sessionFactory.make(socketChannel, null);
        this.regQ.offer(session);
        this.selector.wakeup();
        session.getRegLeach().await();

        socketChannel.connect(remoteAddress);
        session.getLatch().await();


        return session;
    }

    public ReactorPool getReactorPool() {
        return reactorPool;
    }

    public void setReactorPool(ReactorPool reactorPool) {
        this.reactorPool = reactorPool;
    }

    private void handleReg(Selector selector) {
        Session session = null;
        while ((session = this.regQ.poll()) != null) {
            try {
                SelectionKey key = session.getSocketChannel().register(selector, SelectionKey.OP_CONNECT, session);
                session.setKey(key);
                session.getRegLeach().countDown();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }


    @Override
    public void close() throws IOException {
        this.interrupt();
    }
}
