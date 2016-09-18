package io.goudai.net.handler.codec;

import io.goudai.net.buffer.IoBuffer;
import io.goudai.net.handler.serializer.Serializer;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Created by freeman on 2016/1/12.
 */
@RequiredArgsConstructor
public class DefaultDecoder<T> implements Decoder<T> {
    private final int BODY_LEN = 4;
    private final Serializer serializer;


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
            if (obj == null) {
                in.reset();
                return in;
            }
            requests.add(obj);
        }
    }
}
