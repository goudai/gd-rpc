package io.goudai.handler.serializer;

import io.goudai.buffer.IoBuffer;

/**
 * Created by freeman on 2016/1/12.
 */
public interface Serializer {

    IoBuffer encode(Object obj);

    Object decode(byte[] bytes);
}
