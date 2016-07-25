package io.goudai.net.session.factory;

import io.goudai.net.session.AbstractSession;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by freeman on 2016/1/11.
 */
public interface SessionFactory {

    /* 返回一个具体的session实例*/
     AbstractSession make(SocketChannel socketChannel, SelectionKey key);
}
