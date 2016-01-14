package io.goudai.network;

import io.goudai.context.Context;
import io.goudai.context.ContextHolder;
import io.goudai.handler.codec.Decoder;
import io.goudai.handler.codec.DefaultDecoder;
import io.goudai.handler.codec.DefaultEncoder;
import io.goudai.handler.codec.Encoder;
import io.goudai.handler.in.ChannelInHandler;
import io.goudai.handler.serializer.JavaSerializer;
import io.goudai.handler.serializer.Serializer;
import io.goudai.net.Connector2;
import io.goudai.net.ReactorPool;
import io.goudai.session.AbstractSession;
import io.goudai.session.Session;
import io.goudai.session.factory.DefaultSessionFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by Administrator on 2016/1/14.
 */
public class Client {
    static {
        Serializer serializer = new JavaSerializer();
        Decoder<User> decoder = new DefaultDecoder<>(serializer);
        Encoder<User> encoder = new DefaultEncoder<>(serializer);
        ChannelInHandler<User> channelInHandler =  new ChannelInHandler<User>() {
            @Override
            public void received(AbstractSession session, List<User> request) {
                System.out.println("client received on server ");
                request.forEach(System.out::println);
            }
        };
        Context<User, User> context = new Context<>(decoder, encoder, channelInHandler, serializer);
        ContextHolder.registed(context);
    }
    public static void main(String[] args) throws Exception {
        DefaultSessionFactory sessionFactory = new DefaultSessionFactory();
        ReactorPool reactorPool = new ReactorPool(1, sessionFactory);
        reactorPool.startup();
        Connector2 connector = new Connector2("connector-1", reactorPool);
        connector.start();
        Session session = connector.connect(new InetSocketAddress(8888), sessionFactory);
        System.out.println(session);
        session.write(new User());




//        connector.shutdown();
    }
}
