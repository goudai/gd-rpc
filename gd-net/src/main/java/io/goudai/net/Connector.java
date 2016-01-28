package io.goudai.net;

import io.goudai.net.common.Lifecycle;
import io.goudai.net.session.AbstractSession;
import io.goudai.net.session.Session;
import io.goudai.net.session.factory.SessionFactory;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by freeman on 2016/1/8.
 */
@Getter
public class Connector extends Thread implements Lifecycle{
    private static Logger logger = LoggerFactory.getLogger(Connector.class);

    private final Selector selector;
    private final ReactorPool reactorPool;
    private Queue<Session>  asyncSessionQueue = new ConcurrentLinkedQueue<>();


    public Connector(String name,ReactorPool reactorPool) throws IOException {
        super(name);
        this.setDaemon(true);
        this.reactorPool = reactorPool;
        this.selector = Selector.open();
    }

    @Override
    public void startup()  {
        this.start();
        logger.info("connect {} started success",this.getName());
    }

    @Override
    public void shutdown() {
        logger.info("connect {} shutdowning",this.getName());
        try {
            this.selector.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Session connect(InetSocketAddress remoteAddress,long timeout,SessionFactory sessionFactory) throws IOException, InterruptedException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        Session session = (Session) sessionFactory.make(socketChannel,null);
        this.asyncSessionQueue.offer(session);
        socketChannel.connect(remoteAddress);
        this.selector.wakeup();
        session.await(timeout);
        return session;
    }

    @Override
    public void run() {
        while (!interrupted()){
           doSelect();
            if(this.selector.isOpen()){
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                try{
                    selectionKeys.forEach(this::connect);
                }finally {
                    selectionKeys.clear();
                }
            }else return;

        }
    }

    private void connect(SelectionKey key) {
        try {
            if (key.isValid() && key.isConnectable()) {
                AbstractSession session = (AbstractSession) key.attachment();
                    while (!session.getSocketChannel().finishConnect()) {
                        logger.info("check finish connection");
                    }
                    key.interestOps(0);
                    session.finishConnect();
                    reactorPool.register(session.getSocketChannel(), session);
                }else
                 key.cancel();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void handleReg() {
        Session session = null;
        while ((session = this.asyncSessionQueue.poll()) != null) {
            try {
                SelectionKey key = session.getSocketChannel().register(selector, SelectionKey.OP_CONNECT, session);
                session.setKey(key);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
    private void doSelect() {
        try {
            this.selector.select();
            this.handleReg();
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }

    }


}
