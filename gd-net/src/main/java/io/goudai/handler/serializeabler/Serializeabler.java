package io.goudai.handler.serializeabler;

import io.goudai.buffer.IoBuffer;

/**
 * Created by freeman on 2016/1/12.
 */
public interface Serializeabler {

    IoBuffer encode(Object obj);

    Object decode(byte[] bytes);
}
