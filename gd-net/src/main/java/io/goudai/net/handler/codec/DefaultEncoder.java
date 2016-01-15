package io.goudai.net.handler.codec;

import io.goudai.net.buffer.IoBuffer;
import io.goudai.net.handler.serializer.Serializer;

import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2016/1/14.
 */
public class DefaultEncoder<T> implements Encoder<T> {

    private Serializer serializer;

    public DefaultEncoder(Serializer serializer) {
        this.serializer = serializer;
    }

    private final int BODY_LEN = 4;
    @Override
    public ByteBuffer encode(T response) {
        byte[] serialize = serializer.serialize(response);
        int length = serialize.length;
        ByteBuffer buf = IoBuffer.allocate(BODY_LEN + length).writeInt(length).writeBytes(serialize).buf();
        buf.flip();
        return buf;
    }
}
