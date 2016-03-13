package io.goudai.cluster.bootstarp;

import io.goudai.cluster.config.ClusterConfig;
import io.goudai.commons.LifeCycle;
import io.goudai.net.Acceptor;
import io.goudai.net.Reactor;
import io.goudai.net.context.ContextHolder;
import io.goudai.net.session.factory.DefaultSessionFactory;
import io.goudai.rpc.handler.ServiceRegistryHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

/**
 * Created by freeman on 2016/1/28.
 */
@Slf4j
public class ClusterServerBootstrap implements LifeCycle {

    private final static int DEFAULT_REACTOR_NUM = 1;
    private final static CountDownLatch latch = new CountDownLatch(1);
    private Acceptor acceptor;
    private Reactor reactor;

    public ClusterServerBootstrap() throws IOException {
        this(DEFAULT_REACTOR_NUM);
    }

    public ClusterServerBootstrap(int reactors) throws IOException {
        reactor = new Reactor(reactors, new DefaultSessionFactory());
        acceptor = new Acceptor("goudai-rpc-accpector-trhead", new InetSocketAddress(ClusterConfig.port), this.reactor);
    }

    //TODO check service impl intercalss
    public ClusterServerBootstrap registry(Class<?> interClass, Object service) {
        ServiceRegistryHandler channelHandler = (ServiceRegistryHandler) ContextHolder.getContext().getChannelHandler();
        channelHandler.registry(interClass, service);
        return this;
    }

    @Override
    public void startup() {
        this.acceptor.startup();
        this.reactor.startup();
        try {
            log.info("{} started successfully complete !", this);
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void shutdown() {
        latch.countDown();
        this.acceptor.shutdown();
        this.reactor.shutdown();
    }


}
