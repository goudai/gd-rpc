package io.goudai.net.session.factory;

import io.goudai.net.session.AbstractSession;
import io.goudai.net.session.Session;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by freeman on 2016/1/11.
 */
public class DefaultSessionFactory implements SessionFactory {


    @Override
    public AbstractSession make(SocketChannel socketChannel, SelectionKey key) {
        return new Session<>(socketChannel, key);

    }
}
