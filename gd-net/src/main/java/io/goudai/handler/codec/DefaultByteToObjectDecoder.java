package io.goudai.handler.codec;

import io.goudai.buffer.IoBuffer;

import java.util.List;

/**
 * Created by freeman on 2016/1/12.
 */
public class DefaultByteToObjectDecoder<T> implements ByteToObjectDecoder<T> {

    @Override
    public IoBuffer decode(IoBuffer in, List<T> requests) {
        while (true) {
            in.mark();
            if(in.remaining() <= 4) {
                in.reset();
                return in;
            }

            int len = in.readInt();
            if(len > in.remaining() || len <= 0) {
                in.reset();
                return in;
            }

            byte[] body = new byte[len];
            in.readBytes(body);
            // TODO 此处需要进行序列化抽象
            Object obj = null ; //this.handlePacket(body);
            if(obj == null) {
                in.reset();
                return in;
            }
        }
    }
}
