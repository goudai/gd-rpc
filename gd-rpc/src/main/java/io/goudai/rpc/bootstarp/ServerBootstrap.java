package io.goudai.rpc.bootstarp;

import io.goudai.net.Acceptor;
import io.goudai.net.ReactorPool;
import io.goudai.net.common.Lifecycle;
import io.goudai.net.context.ContextHolder;
import io.goudai.net.session.factory.DefaultSessionFactory;
import io.goudai.rpc.handler.RequestHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by freeman on 2016/1/28.
 */
public class ServerBootstrap implements Lifecycle {

    private Acceptor acceptor;
    private ReactorPool reactorPool;
    private int port;
    private final static int DEFAULT_REACTOR_NUM = 1;

    public ServerBootstrap(int port) throws IOException {
        this(DEFAULT_REACTOR_NUM, port);
    }

    public ServerBootstrap(int reactors, int port) throws IOException {
        reactorPool = new ReactorPool(reactors, new DefaultSessionFactory());
        acceptor = new Acceptor("goudai-rpc-accpector-trhead", new InetSocketAddress(port), this.reactorPool);
        this.port = port;
    }
    //TODO check service impl intercalss
    public ServerBootstrap registry(Class<?> interClass, Object service) {
        RequestHandler channelHandler = (RequestHandler) ContextHolder.getContext().getChannelHandler();
        channelHandler.service(interClass, service);
        return this;
    }

    @Override
    public void startup()   {
        this.acceptor.startup();
        this.reactorPool.startup();
    }

    @Override
    public void shutdown()   {
        this.acceptor.shutdown();
        this.reactorPool.shutdown();
    }



}
