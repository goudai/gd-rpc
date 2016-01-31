package io.goudai.rpc.bootstarp;

import io.goudai.net.Connector;
import io.goudai.net.ReactorPool;
import io.goudai.net.common.Lifecycle;
import io.goudai.net.session.factory.DefaultSessionFactory;
import io.goudai.rpc.exception.RpcException;
import io.goudai.rpc.invoker.RequestSessionFactory;
import io.goudai.rpc.invoker.SingleInvoker;
import io.goudai.rpc.listener.SessionManager;
import io.goudai.rpc.proxy.JavaProxyServiceFactory;
import io.goudai.rpc.proxy.ProxyServiceFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Created by vip on 2016/1/28.
 */
@Slf4j
public class Bootstrap implements Lifecycle {
    private Connector connector;
    private ReactorPool reactorPool;
    private String serverIp;
    private int serverPort;
    private ProxyServiceFactory proxyServiceFactory;
    private final DefaultSessionFactory sessionFactory = new DefaultSessionFactory();

    public Bootstrap(String serverIp, int serverPort,int reactors) throws IOException {
        reactorPool = new ReactorPool(reactors, sessionFactory);
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
        proxyServiceFactory = new JavaProxyServiceFactory(new SingleInvoker(new RequestSessionFactory(serverIp, serverPort,connector,sessionFactory)));
    }

    @Override
    public void shutdown() {
        try {
            SessionManager.getInstance().close();
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e);
        }
        this.connector.shutdown();
        log.info("[{}] shutdown connector complete",this.connector);
        this.reactorPool.shutdown();
        log.info("[{}] shutdown reactorPool complete",this.reactorPool);
    }
}
