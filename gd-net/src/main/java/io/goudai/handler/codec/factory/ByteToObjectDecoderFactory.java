package io.goudai.handler.codec.factory;

import io.goudai.handler.codec.ByteToObjectDecoder;

/**
 * Created by freeman on 2016/1/12.
 */
public interface ByteToObjectDecoderFactory<T> {

    ByteToObjectDecoder<T> make();
}
