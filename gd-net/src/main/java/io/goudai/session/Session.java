package io.goudai.session;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by freeman on 2016/1/8.
 */
public class Session extends AbstractSession{


    public Session(SocketChannel socketChannel, SelectionKey key) {
        super(socketChannel, key);
    }
    //TODO 实现具体的读
    @Override
    public void read() throws IOException {

    }
    //TODO 实现具体的写
    @Override
    public void realWrite() throws IOException {

    }
    //TODO 此处讲写入Queue 是否可以抽象到父类？
    @Override
    public void write(byte[] bytes) throws IOException {

    }


}
