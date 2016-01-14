package io.goudai.session.factory;

import io.goudai.context.ContextHolder;
import io.goudai.session.AbstractSession;
import io.goudai.session.Session;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by freeman on 2016/1/11.
 */
public class DefaultSessionFactory implements SessionFactory {


    @Override
    public AbstractSession make(SocketChannel socketChannel, SelectionKey key) {
        return new Session<>(socketChannel,key, ContextHolder.getContext());
    }
}
