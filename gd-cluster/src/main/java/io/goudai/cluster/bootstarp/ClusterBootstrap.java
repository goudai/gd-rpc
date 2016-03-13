package io.goudai.cluster.bootstarp;

import io.goudai.cluster.balance.Balance;
import io.goudai.cluster.invoker.KeyPooledClusterInvoker;
import io.goudai.net.Connector;
import io.goudai.net.Reactor;
import io.goudai.net.session.factory.DefaultSessionFactory;
import io.goudai.registry.Registry;
import io.goudai.registry.ZooKeeRegistry;
import io.goudai.rpc.bootstarp.Bootstrap;
import io.goudai.rpc.exception.RpcException;
import io.goudai.rpc.invoker.Invoker;
import io.goudai.rpc.listener.SessionManager;
import io.goudai.rpc.proxy.JavaProxyServiceFactory;
import io.goudai.rpc.proxy.ProxyServiceFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Created by freeman on 2016/1/28.
 */
@Slf4j
public class ClusterBootstrap implements Bootstrap {
    private final DefaultSessionFactory sessionFactory = new DefaultSessionFactory();
    private Connector connector;
    private Reactor reactor;
    private Registry registry;
    private ProxyServiceFactory proxyServiceFactory;

    public ClusterBootstrap(Registry registry, int reactors) throws IOException {
        reactor = new Reactor(reactors, sessionFactory);
        connector = new Connector("goudai-rpc-connector-thread", reactor);
        this.registry = registry;
    }

    public <T> T getService(Class<T> clazz) {
        return proxyServiceFactory.createServiceProxy(clazz);
    }

    @Override
    public void startup() {
        this.connector.startup();
        this.reactor.startup();
        this.registry.startup();
        Invoker invoker = new KeyPooledClusterInvoker(new ZooKeeRegistry(), new Balance() {},connector);
        proxyServiceFactory = new JavaProxyServiceFactory(invoker);
    }

    @Override
    public void shutdown() {
        try {
            SessionManager.getInstance().close();
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e);
        }
        this.connector.shutdown();
        log.info("[{}] shutdown connector complete", this.connector);
        this.reactor.shutdown();
        log.info("[{}] shutdown reactorPool complete", this.reactor);
        registry.shutdown();

    }
}
