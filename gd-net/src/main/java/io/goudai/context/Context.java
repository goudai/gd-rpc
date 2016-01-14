package io.goudai.context;

import io.goudai.handler.codec.Decoder;
import io.goudai.handler.codec.Encoder;
import io.goudai.handler.in.ChannelInHandler;
import io.goudai.handler.serializer.Serializer;

/**
 * Created by freeman on 2016/1/12.
 */

public class Context<REQ, RESP> {


    private final Decoder<REQ> decoder;
    private final Encoder<RESP> encoder;
    private final ChannelInHandler<REQ> channelHanlder;
    private final Serializer serializer;

    public Context(Decoder<REQ> decoder, Encoder<RESP> encoder, ChannelInHandler<REQ> channelHanlder, Serializer serializer) {
        this.decoder = decoder;
        this.encoder = encoder;
        this.channelHanlder = channelHanlder;
        this.serializer = serializer;
    }

    public Decoder<REQ> getDecoder() {
        return decoder;
    }


    public Encoder<RESP> getEncoder() {
        return encoder;
    }


    public ChannelInHandler<REQ> getChannelHanlder() {
        return channelHanlder;
    }


    public Serializer getSerializer() {
        return serializer;
    }

}
