package io.goudai.handler.codec.factory;

import io.goudai.handler.codec.Decoder;

/**
 * Created by freeman on 2016/1/12.
 */
public interface DecoderFactory<T> {

    Decoder<T> make();
}
