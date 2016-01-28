package io.goudai.rpc.bootstarp;

import io.goudai.commons.pool.PoolConfig;
import io.goudai.net.Connector;
import io.goudai.net.ReactorPool;
import io.goudai.net.common.Lifecycle;
import io.goudai.net.session.factory.DefaultSessionFactory;
import io.goudai.rpc.exception.RpcException;
import io.goudai.rpc.invoker.RequestSession;
import io.goudai.rpc.invoker.SingleInvoker;
import io.goudai.rpc.proxy.JavaProxyServiceFactory;
import io.goudai.rpc.proxy.ProxyServiceFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by vip on 2016/1/28.
 */
public class Bootstrap implements Lifecycle {
    private Connector connector;
    private ReactorPool reactorPool;
    private ProxyServiceFactory proxyServiceFactory;
    private String serverIp;
    private int serverPort;
    private final DefaultSessionFactory sessionFactory = new DefaultSessionFactory();

    public Bootstrap(String serverIp, int serverPort) throws IOException {

        reactorPool = new ReactorPool(1, sessionFactory);
        connector = new Connector("goudai-rpc-connector-thread", reactorPool);
        this.serverIp = serverIp;
        this.serverPort = serverPort;

    }

    public <T> T getService(Class<T> clazz) {
        return proxyServiceFactory.createServiceProxy(clazz);
    }

    @Override
    public void startup() {
        this.connector.startup();
        this.reactorPool.startup();
        proxyServiceFactory = new JavaProxyServiceFactory(new SingleInvoker(() -> {
            try {
                return new RequestSession(new InetSocketAddress(serverIp, serverPort), connector, sessionFactory);
            } catch (Exception e) {
                throw new RpcException(e.getMessage(), e);
            }
        }, PoolConfig.builder().max(30).init(30).build()));
    }

    @Override
    public void shutdown() {
        this.connector.shutdown();
        this.reactorPool.shutdown();
    }
}
