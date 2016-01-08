package io.goudai.net;

import io.goudai.common.Life;
import io.goudai.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Set;

/**
 * Created by freeman on 2016/1/8.
 */
public class Connector extends Thread implements Life{
    private static Logger logger = LoggerFactory.getLogger(Connector.class);

    private final Selector selector;

    private ReactorPool reactorPool;

    private InetSocketAddress remoteServerAddress;

    public Connector(String name, InetSocketAddress remoteServerAddress, ReactorPool reactorPool) throws IOException {
        super(name);
        this.reactorPool = reactorPool;
        this.selector = Selector.open();
        this.remoteServerAddress = remoteServerAddress;
    }

    @Override
    public void startup() throws Exception {
        this.start();
        logger.info("connect {} started success",this.getName());
    }

    @Override
    public void shutdown() throws Exception {
        logger.info("connect {} shutdowning",this.getName());
        this.selector.close();
    }

    public Session connect(InetSocketAddress remoteAddress) throws IOException, InterruptedException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        Session session = new Session(socketChannel, null, new Date());
        this.selector.wakeup();
        session.getRegLeach().await();

        socketChannel.connect(remoteAddress);
        session.getLatch().await();


        return session;
    }

    @Override
    public void run() {
        while (!interrupted()){
           doSelect();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            try{
                selectionKeys.forEach(this::connect);
            }finally {
                selectionKeys.clear();
            }
        }
    }

    private void connect(SelectionKey key) {
        try {
            if (key.isValid() && key.isConnectable()) {
                    Session session = (Session) key.attachment();
                    while (!session.getSocketChannel().finishConnect()) {
                        logger.info("check finish connection");
                    }
                    key.interestOps(0);
                    key.cancel();
                    session.getRegLeach().countDown();
                    reactorPool.register(session.getSocketChannel(), session);
                } else {
                    key.cancel();
                }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void doSelect() {
        try {
            this.selector.select();
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }

    }


}
