package io.goudai.rpc;

import io.goudai.commons.pool.PoolConfig;
import io.goudai.net.Connector;
import io.goudai.net.ReactorPool;
import io.goudai.net.session.factory.DefaultSessionFactory;
import io.goudai.rpc.exception.RpcException;
import io.goudai.rpc.invoker.Invoker;
import io.goudai.rpc.invoker.RequestSession;
import io.goudai.rpc.invoker.SingleInvoker;
import io.goudai.rpc.proxy.JavaProxyServiceFactory;
import io.goudai.rpc.proxy.ProxyServiceFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by Administrator on 2016/1/17.
 */
public class ProxyServiceTest {
    public static void main(String[] args) throws IOException {
        DefaultSessionFactory sessionFactory = new DefaultSessionFactory();
        ReactorPool reactorPool = new ReactorPool(1, sessionFactory);
        Connector connector = new Connector("rpc-client", reactorPool);
        Invoker singleInvoker = new SingleInvoker(30, () -> {
            try {
                return new RequestSession(new InetSocketAddress(888), connector, sessionFactory);
            } catch (Exception e) {
                throw new RpcException(e.getMessage(), e);
            }
        }, new PoolConfig());
        ProxyServiceFactory proxyServiceFactory = new JavaProxyServiceFactory(singleInvoker);
        UserService serviceProxy = proxyServiceFactory.createServiceProxy(UserService.class);
        //进行userService的方法调用
    }
}
