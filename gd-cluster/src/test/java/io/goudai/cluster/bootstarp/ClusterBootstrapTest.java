package io.goudai.cluster.bootstarp;

import io.goudai.cluster.ClusterBootstrap;
import io.goudai.cluster.domain.User;
import io.goudai.cluster.domain.UserService;
import io.goudai.cluster.config.ClusterConfig;
import io.goudai.commons.factory.NamedThreadFactory;
import io.goudai.net.context.Context;
import io.goudai.net.handler.codec.DefaultDecoder;
import io.goudai.net.handler.codec.DefaultEncoder;
import io.goudai.net.handler.serializer.JavaSerializer;
import io.goudai.net.handler.serializer.Serializer;
import io.goudai.registry.ZooKeeRegistry;
import io.goudai.rpc.handler.ResponseHandler;
import io.goudai.rpc.listener.RpcListener;
import io.goudai.rpc.model.Request;
import io.goudai.rpc.model.Response;

import java.util.concurrent.Executors;

/**
 * Created by freeman on 2016/1/28.
 */
public class ClusterBootstrapTest {
    static {
        //1 init
        Serializer serializer = new JavaSerializer();
        Context.<Request, Response>builder()
                .decoder(new DefaultDecoder<>(serializer))
                .encoder(new DefaultEncoder<>(serializer))
                .serializer(serializer)
                .channelHandler(new ResponseHandler())
                .sessionListener(new RpcListener())
                .executorService(Executors.newFixedThreadPool(20, new NamedThreadFactory("goudai-rpc-works", true)))
                .build()
                .init();
    }

    public static void main(String[] args) throws Exception {

        ClusterConfig.application = "myApp";
        //2 create client
        ZooKeeRegistry registry = new ZooKeeRegistry();
        ClusterBootstrap clusterBootstrap = new ClusterBootstrap(registry, 1);
        //3 started client
        clusterBootstrap.startup();
        //4 get proxy service
        UserService service = clusterBootstrap.getService(UserService.class);
        //5 remote invoker
        User add = service.add(new User());
        // out result
        System.err.println(add);
        //7 shutdown
        clusterBootstrap.shutdown();

    }
}
