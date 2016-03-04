package io.goudai.rpc;

import io.goudai.commons.factory.NamedThreadFactory;
import io.goudai.net.Acceptor;
import io.goudai.net.Reactor;
import io.goudai.net.context.Context;
import io.goudai.net.context.ContextHolder;
import io.goudai.net.handler.codec.DefaultDecoder;
import io.goudai.net.handler.codec.DefaultEncoder;
import io.goudai.net.handler.serializer.JavaSerializer;
import io.goudai.net.handler.serializer.Serializer;
import io.goudai.net.session.factory.DefaultSessionFactory;
import io.goudai.rpc.handler.RequestHandler;
import io.goudai.rpc.model.Request;
import io.goudai.rpc.model.Response;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created by vip on 2016/1/21.
 */
public class ProxyServiceServerTest {
    static {
        Serializer serializer = new JavaSerializer();
        Context.<Request, Response>builder()
                .decoder(new DefaultDecoder<>(serializer))
                .encoder(new DefaultEncoder<>(serializer))
                .serializer(serializer)
                .channelHandler(new RequestHandler())
                .executorService(Executors.newFixedThreadPool(20, new NamedThreadFactory()))
                .build()
                .init();
    }
    public static void main(String[] args) throws Exception {
        RequestHandler handler = (RequestHandler) ContextHolder.getContext().getChannelHandler();
        handler.registry(UserService.class,new SimpleUserService());
        DefaultSessionFactory sessionFactory = new DefaultSessionFactory();
        Reactor reactor = new Reactor(1, sessionFactory);
        reactor.startup();
        Acceptor acceptor = new Acceptor("rpc-server",new InetSocketAddress("0.0.0.0",9999), reactor);
        acceptor.startup();
        while (true){
            Thread.sleep(Long.MAX_VALUE);
        }

    }
}
