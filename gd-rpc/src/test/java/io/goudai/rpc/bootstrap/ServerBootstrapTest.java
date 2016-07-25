package io.goudai.rpc.bootstrap;

import io.goudai.commons.factory.NamedThreadFactory;
import io.goudai.net.context.Context;
import io.goudai.net.handler.codec.DefaultDecoder;
import io.goudai.net.handler.codec.DefaultEncoder;
import io.goudai.net.handler.serializer.JavaSerializer;
import io.goudai.net.handler.serializer.Serializer;
import io.goudai.net.session.AbstractSessionListener;
import io.goudai.rpc.SimpleUserService;
import io.goudai.rpc.UserService;
import io.goudai.rpc.bootstarp.ServerBootstrap;
import io.goudai.rpc.handler.RequestHandler;
import io.goudai.rpc.model.Request;
import io.goudai.rpc.model.Response;

import java.util.concurrent.Executors;

/**
 * Created by freeman on 2016/1/28.
 */
public class ServerBootstrapTest {
    static {
        //1 init context
        Serializer serializer = new JavaSerializer();
        Context.<Request, Response>builder()
                .decoder(new DefaultDecoder<>(serializer))
                .encoder(new DefaultEncoder<>(serializer))
                .serializer(serializer)
                .channelHandler(new RequestHandler())
                .sessionListener(new AbstractSessionListener())
                .executorService(Executors.newFixedThreadPool(200, new NamedThreadFactory()))
                .build()
                .init();
    }

    public static void main(String[] args) throws Exception {
        // 2 init rpc server
        ServerBootstrap serverBootstrap = new ServerBootstrap(2,9999);
        //3 registry shutdown clean hook
        Runtime.getRuntime().addShutdownHook(new Thread(serverBootstrap::shutdown));
        //4 registry services..
        serverBootstrap.registry(UserService.class, new SimpleUserService());
        //5 started rpc server and await thread
        serverBootstrap.startup();


    }
}
