package io.goudai.session;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Date;

/**
 * Created by freeman on 2016/1/8.
 */
public class Session extends AbstractSession{


    public Session(SocketChannel socketChannel, SelectionKey key, Date createdTime) {
        super(socketChannel, key, createdTime);
    }

    @Override
    public void read() throws IOException {

    }

    @Override
    public void realWrite() throws IOException {

    }

    @Override
    public void write(byte[] bytes) throws IOException {

    }
}
