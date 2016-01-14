package io.goudai.handler.codec;

import io.goudai.buffer.IoBuffer;
import io.goudai.handler.serializer.Serializer;

import java.util.List;

/**
 * Created by freeman on 2016/1/12.
 */
public class DefaultDecoder<T> implements Decoder<T> {
    private final int BODY_LEN = 4;
    private Serializer serializer;

    public DefaultDecoder(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public IoBuffer decode(IoBuffer in, List<T> requests) {
        while (true) {
            in.mark();
            if (in.remaining() <= BODY_LEN) {
                in.reset();
                return in;
            }

            int len = in.readInt();
            if (len > in.remaining() || len <= 0) {
                in.reset();
                return in;
            }

            byte[] body = new byte[len];
            in.readBytes(body);
            T obj = (T) serializer.deserialize(body);
            requests.add(obj);
            if (obj == null) {
                in.reset();
                return in;
            }
        }
    }
}
