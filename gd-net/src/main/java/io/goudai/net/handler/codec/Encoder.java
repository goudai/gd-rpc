package io.goudai.net.handler.codec;

import java.nio.ByteBuffer;

public interface Encoder<T> {

    ByteBuffer encode(T response);

}
