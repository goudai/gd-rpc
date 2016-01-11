package io.goudai.session.factory;

import io.goudai.session.AbstractSession;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by freeman on 2016/1/11.
 */
public class DefaultSessionFactory implements SessionFactory {
    //TODO 返回具体的session实例
    @Override
    public AbstractSession make(SocketChannel socketChannel, SelectionKey key) {
        return null;
    }
}
