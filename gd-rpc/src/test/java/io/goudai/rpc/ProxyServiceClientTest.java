package io.goudai.rpc;

import io.goudai.commons.factory.NamedThreadFactory;
import io.goudai.commons.pool.PoolConfig;
import io.goudai.net.Connector;
import io.goudai.net.ReactorPool;
import io.goudai.net.context.Context;
import io.goudai.net.handler.codec.DefaultDecoder;
import io.goudai.net.handler.codec.DefaultEncoder;
import io.goudai.net.handler.serializer.JavaSerializer;
import io.goudai.net.handler.serializer.Serializer;
import io.goudai.net.session.factory.DefaultSessionFactory;
import io.goudai.rpc.exception.RpcException;
import io.goudai.rpc.handler.ResponseHandler;
import io.goudai.rpc.invoker.Invoker;
import io.goudai.rpc.invoker.RequestSession;
import io.goudai.rpc.invoker.SingleInvoker;
import io.goudai.rpc.model.Request;
import io.goudai.rpc.model.Response;
import io.goudai.rpc.proxy.JavaProxyServiceFactory;
import io.goudai.rpc.proxy.ProxyServiceFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2016/1/17.
 */
public class ProxyServiceClientTest {
    static {
        Serializer serializer = new JavaSerializer();
        Context.<Request, Response>builder()
                .decoder(new DefaultDecoder<>(serializer))
                .encoder(new DefaultEncoder<>(serializer))
                .serializer(serializer)
                .channelHandler(new ResponseHandler())
                .executorService(Executors.newFixedThreadPool(20, new NamedThreadFactory()))
                .build()
                .init();
    }
    public static void main(String[] args) throws Exception {
        DefaultSessionFactory sessionFactory = new DefaultSessionFactory();
        ReactorPool reactorPool = new ReactorPool(1, sessionFactory);
        reactorPool.startup();
        Connector connector = new Connector("rpc-client", reactorPool);
        connector.startup();
        Invoker singleInvoker = new SingleInvoker(() -> {
            try {
                return new RequestSession(new InetSocketAddress(8888), connector, sessionFactory);
            } catch (Exception e) {
                throw new RpcException(e.getMessage(), e);
            }
        }, PoolConfig.builder().max(30).init(30).build());
        ProxyServiceFactory proxyServiceFactory = new JavaProxyServiceFactory(singleInvoker);
        UserService serviceProxy = proxyServiceFactory.createServiceProxy(UserService.class);
        User add = serviceProxy.add(new User());
        System.out.println(serviceProxy);
//        UserService serviceProxy = proxyServiceFactory.createServiceProxy(UserService.class);
//        System.out.println(serviceProxy);
        //进行userService的方法调用
    }
}
