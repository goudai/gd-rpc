package io.goudai.net;

import io.goudai.net.common.Lifecycle;
import io.goudai.net.session.AbstractSession;
import io.goudai.net.session.factory.SessionFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by freeman on 2016/1/8.
 */
@Slf4j
public class ReactorPool implements Lifecycle {


    private final Reactor[] reactors;
    private final int reactorCount;
    private final static AtomicInteger selectorIndex = new AtomicInteger();


    public ReactorPool(int reactorCount,SessionFactory sessionFactory) throws IOException {
        if (reactorCount <= 0) {
            reactorCount = 1;
            log.warn("reactor less than 1,using auto reactors 1");
        }
        this.reactors = new Reactor[reactorCount];
        for (int i = 0; i < this.reactors.length; i++) {
            this.reactors[i] = new Reactor("reactor--" + i,sessionFactory);
            log.info("init reactor pool [{}] = [{}]",i,this.reactors[i]);
        }
        this.reactorCount = reactorCount;
    }

    @Override
    public void startup() throws Exception {
        for (Lifecycle life : this.reactors) {
            life.startup();
        }
    }

    @Override
    public void shutdown() throws Exception {
        for (Lifecycle life : this.reactors) {
            life.shutdown();
        }
    }

    public void register(SocketChannel socketChannel) throws ClosedChannelException {
        this.register(socketChannel,null);
    }

    public void register(SocketChannel socketChannel, AbstractSession session) throws ClosedChannelException {
        this.nextReactor().register(socketChannel, SelectionKey.OP_READ,session);
    }



    private Reactor nextReactor() {
        selectorIndex.compareAndSet(Integer.MAX_VALUE, 0);
        return this.reactors[this.selectorIndex.getAndIncrement() % this.reactorCount];
    }

}
