package io.goudai.net.context;

import io.goudai.net.handler.codec.Decoder;
import io.goudai.net.handler.codec.Encoder;
import io.goudai.net.handler.in.ChannelInHandler;
import io.goudai.net.handler.serializer.Serializer;

import java.util.concurrent.ExecutorService;

/**
 * Created by freeman on 2016/1/12.
 */

public class Context<REQ, RESP> {


    private final Decoder<REQ> decoder;
    private final Encoder<RESP> encoder;
    private final ChannelInHandler<REQ> channelInHandler;
    private final Serializer serializer;
    private final ExecutorService executorService;

    public Context(Decoder<REQ> decoder, Encoder<RESP> encoder, ChannelInHandler<REQ> channelInHandler, Serializer serializer, ExecutorService executorService) {
        this.decoder = decoder;
        this.encoder = encoder;
        this.channelInHandler = channelInHandler;
        this.serializer = serializer;
        this.executorService = executorService;
    }

    public Decoder<REQ> getDecoder() {
        return decoder;
    }


    public Encoder<RESP> getEncoder() {
        return encoder;
    }


    public ChannelInHandler<REQ> getChannelInHandler() {
        return channelInHandler;
    }


    public Serializer getSerializer() {
        return serializer;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

}
