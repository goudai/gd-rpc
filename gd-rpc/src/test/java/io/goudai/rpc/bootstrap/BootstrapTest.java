package io.goudai.rpc.bootstrap;

import io.goudai.commons.factory.NamedThreadFactory;
import io.goudai.net.context.Context;
import io.goudai.net.handler.codec.DefaultDecoder;
import io.goudai.net.handler.codec.DefaultEncoder;
import io.goudai.net.handler.serializer.JavaSerializer;
import io.goudai.net.handler.serializer.Serializer;
import io.goudai.rpc.User;
import io.goudai.rpc.UserService;
import io.goudai.rpc.bootstarp.Bootstrap;
import io.goudai.rpc.handler.ResponseHandler;
import io.goudai.rpc.model.Request;
import io.goudai.rpc.model.Response;

import java.util.concurrent.Executors;

/**
 * Created by vip on 2016/1/28.
 */
public class BootstrapTest {
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
        Bootstrap bootstrap = new Bootstrap("localhost", 9999);

        bootstrap.startup();

        UserService service = bootstrap.getService(UserService.class);
        User add = service.add(new User());
        System.out.println(add);

        Runtime.getRuntime().addShutdownHook(new Thread(bootstrap::shutdown));

    }
}