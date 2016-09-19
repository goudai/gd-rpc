package io.goudai.cluster.bootstarp;

import io.goudai.cluster.config.ClusterConfig;
import io.goudai.cluster.domain.SimpleUserService;
import io.goudai.cluster.domain.UserService;
import io.goudai.cluster.handler.ClusterRequestHandler;
import io.goudai.commons.factory.NamedThreadFactory;
import io.goudai.net.context.Context;
import io.goudai.net.handler.codec.DefaultDecoder;
import io.goudai.net.handler.codec.DefaultEncoder;
import io.goudai.net.handler.serializer.Serializer;
import io.goudai.net.session.AbstractSessionListener;
import io.goudai.registry.ZooKeeRegistry;
import io.goudai.rpc.model.Request;
import io.goudai.rpc.model.Response;

import java.util.concurrent.Executors;

/**
 * Created by freeman on 2016/3/7.
 */
public class ServerBootTest {


    public static void main(String[] args) throws Exception {
        ClusterConfig.application = "myApp";
        //1 init context
        Serializer serializer = new JsonSerizable();
        ZooKeeRegistry registry = new ZooKeeRegistry();
        registry.startup();
        Context.<Request, Response>builder()
                .decoder(new DefaultDecoder<>(serializer))
                .encoder(new DefaultEncoder<>(serializer))
                .serializer(serializer)
                .channelHandler(new ClusterRequestHandler(registry))
                .sessionListener(new AbstractSessionListener())
                .executorService(Executors.newFixedThreadPool(200, new NamedThreadFactory()))
                .build()
                .init();
        ClusterConfig.port = 6161;

        // 2 init rpc server
        ClusterServerBootstrap serverBootstrap = new ClusterServerBootstrap(1);
        //4 registry services..
        serverBootstrap.registry(UserService.class, new SimpleUserService());
        //3 registry shutdown clean hook
        Runtime.getRuntime().addShutdownHook(new Thread(serverBootstrap::shutdown));
        //5 started rpc server and await thread
        serverBootstrap.startup();
    }
}
