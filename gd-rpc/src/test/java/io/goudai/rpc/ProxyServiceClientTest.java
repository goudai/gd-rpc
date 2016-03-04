package io.goudai.rpc;

import io.goudai.commons.factory.NamedThreadFactory;
import io.goudai.net.Connector;
import io.goudai.net.Reactor;
import io.goudai.net.context.Context;
import io.goudai.net.handler.codec.DefaultDecoder;
import io.goudai.net.handler.codec.DefaultEncoder;
import io.goudai.net.handler.serializer.JavaSerializer;
import io.goudai.net.handler.serializer.Serializer;
import io.goudai.net.session.factory.DefaultSessionFactory;
import io.goudai.rpc.handler.ResponseHandler;
import io.goudai.rpc.invoker.RequestSessionFactory;
import io.goudai.rpc.invoker.Invoker;
import io.goudai.rpc.invoker.SingleInvoker;
import io.goudai.rpc.model.Request;
import io.goudai.rpc.model.Response;
import io.goudai.rpc.proxy.JavaProxyServiceFactory;
import io.goudai.rpc.proxy.ProxyServiceFactory;

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
        Reactor reactor = new Reactor(1, sessionFactory);
        reactor.startup();
        Connector connector = new Connector("rpc-client", reactor);
        connector.startup();
        Invoker singleInvoker = new SingleInvoker(new RequestSessionFactory("localhost", 9999,connector,sessionFactory));
        ProxyServiceFactory proxyServiceFactory = new JavaProxyServiceFactory(singleInvoker);
        UserService serviceProxy = proxyServiceFactory.createServiceProxy(UserService.class);
        User add = serviceProxy.add(new User());
        System.out.println(serviceProxy);
//        UserService serviceProxy = proxyServiceFactory.createServiceProxy(UserService.class);
//        System.out.println(serviceProxy);
        //进行userService的方法调用
    }
}
