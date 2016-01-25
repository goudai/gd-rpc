package io.goudai.network;

import io.goudai.commons.factory.NamedThreadFactory;
import io.goudai.net.Connector;
import io.goudai.net.ReactorPool;
import io.goudai.net.context.Context;
import io.goudai.net.handler.codec.DefaultDecoder;
import io.goudai.net.handler.codec.DefaultEncoder;
import io.goudai.net.handler.serializer.JavaSerializer;
import io.goudai.net.handler.serializer.Serializer;
import io.goudai.net.session.Session;
import io.goudai.net.session.factory.DefaultSessionFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created by freeman on 2016/1/14.
 */
public class Client {
    static {
        Serializer serializer = new JavaSerializer();
        Context.<User, User>builder()
                .decoder(new DefaultDecoder<>(serializer))
                .encoder(new DefaultEncoder<>(serializer))
                .serializer(serializer)
                .channelHandler((session, request) -> {
                    System.out.println("client received on server ");
                    System.out.println(request);
                })
                .executorService(Executors.newFixedThreadPool(20, new NamedThreadFactory()));

    }

    public static void main(String[] args) throws Exception {
        DefaultSessionFactory sessionFactory = new DefaultSessionFactory();
        ReactorPool reactorPool = new ReactorPool(1, sessionFactory);
        reactorPool.startup();
        Connector connector = new Connector("connector-1", reactorPool);
        connector.start();
        Session session = connector.connect(new InetSocketAddress(8888), 4000, sessionFactory);
        System.out.println(session);
        session.write(new User());


//        connector.shutdown();
    }
}
