package io.goudai.network;

import io.goudai.commons.factory.NamedThreadFactory;
import io.goudai.net.Acceptor;
import io.goudai.net.Reactor;
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
                .channelHandler((session, request) -> {
                    System.out.println("server received :--");
                    System.out.println(request);
                    session.write(new User());
                })
                .executorService(Executors.newFixedThreadPool(20, new NamedThreadFactory()))
                .build()
                .init();
    }

    public static void main(String[] args) throws Exception {

        Reactor reactor = new Reactor(1, new DefaultSessionFactory());
        reactor.startup();
        Acceptor acceptor = new Acceptor("test-server", new InetSocketAddress(8888), reactor);
        acceptor.startup();
    }
}
