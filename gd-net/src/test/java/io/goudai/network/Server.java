package io.goudai.network;

import io.goudai.common.Life;
import io.goudai.context.Context;
import io.goudai.context.ContextHolder;
import io.goudai.handler.codec.Decoder;
import io.goudai.handler.codec.DefaultDecoder;
import io.goudai.handler.codec.DefaultEncoder;
import io.goudai.handler.codec.Encoder;
import io.goudai.handler.in.ChannelInHandler;
import io.goudai.handler.serializer.JavaSerializer;
import io.goudai.handler.serializer.Serializer;
import io.goudai.net.Acceptor;
import io.goudai.net.ReactorPool;
import io.goudai.session.AbstractSession;
import io.goudai.session.factory.DefaultSessionFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by Administrator on 2016/1/14.
 */
public class Server {
    static {
        Serializer serializer = new JavaSerializer();
        Decoder<User> decoder = new DefaultDecoder<>(serializer);
        Encoder<User> encoder = new DefaultEncoder<>(serializer);
        ChannelInHandler<User> channelInHandler = new ChannelInHandler<User>() {
            @Override
            public void received(AbstractSession session, List<User> request) {
                System.out.println("server received :--");
                request.forEach(System.out::println);
                session.write(new User());
            }
        };
        Context<User, User> context = new Context<>(decoder, encoder, channelInHandler, serializer);
        ContextHolder.registed(context);
    }

    public static void main(String[] args) throws Exception {

        ReactorPool reactorPool = new ReactorPool(1, new DefaultSessionFactory());
        reactorPool.startup();
        Life acceptor = new Acceptor("test-server", new InetSocketAddress(8888), reactorPool);
        acceptor.startup();
    }
}
