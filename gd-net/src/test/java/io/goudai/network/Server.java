package io.goudai.network;

import io.goudai.commons.factory.NamedThreadFactory;
import io.goudai.net.Acceptor;
import io.goudai.net.ReactorPool;
import io.goudai.net.context.Context;
import io.goudai.net.handler.codec.DefaultDecoder;
import io.goudai.net.handler.codec.DefaultEncoder;
import io.goudai.net.handler.serializer.JavaSerializer;
import io.goudai.net.handler.serializer.Serializer;
import io.goudai.net.session.factory.DefaultSessionFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created by freeman on 2016/1/14.
 */
public class Server {

    static {
        Serializer serializer = new JavaSerializer();
        Context.<User, User>builder()
                .decoder(new DefaultDecoder<>(serializer))
                .encoder(new DefaultEncoder<>(serializer))
                .serializer(serializer)
                .channelInHandler((session, request) -> {
                    System.out.println("server received :--");
                    System.out.println(request);
                    session.write(new User());
                })
                .executorService(Executors.newFixedThreadPool(20, new NamedThreadFactory()))
                .init();
    }

    public static void main(String[] args) throws Exception {

        ReactorPool reactorPool = new ReactorPool(1, new DefaultSessionFactory());
        reactorPool.startup();
        Acceptor acceptor = new Acceptor("test-server", new InetSocketAddress(8888), reactorPool);
        acceptor.startup();
    }
}
