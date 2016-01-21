package io.goudai.net.context;

import io.goudai.commons.util.Assert;
import io.goudai.net.handler.ChannelHandler;
import io.goudai.net.handler.codec.Decoder;
import io.goudai.net.handler.codec.Encoder;
import io.goudai.net.handler.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * Created by freeman on 2016/1/12.
 */

public class Context<REQ, RESP> {

    private final static Logger logger = LoggerFactory.getLogger(Context.class);

    private final Decoder<REQ> decoder;
    private final Encoder<RESP> encoder;
    private ChannelHandler channelInHandler;
    private final Serializer serializer;
    private final ExecutorService executorService;


    public static <REQ, RESP> ContextBuilder<REQ, RESP> builder() {
        return new ContextBuilder<>();
    }

    private Context(Decoder<REQ> decoder, Encoder<RESP> encoder, Serializer serializer, ExecutorService executorService,ChannelHandler channelHandler) {
        this.decoder = decoder;
        this.encoder = encoder;
        this.serializer = serializer;
        this.executorService = executorService;
        this.channelInHandler = channelHandler;
    }

    public static class ContextBuilder<REQ, RESP> {
        Decoder<REQ> decoder;
        Encoder<RESP> encoder;
        ChannelHandler channelInHandler;

        Serializer serializer;
        ExecutorService executorService;

        public ContextBuilder<REQ, RESP> decoder(Decoder<REQ> decoder) {
            this.decoder = decoder;
            return this;
        }

        public ContextBuilder<REQ, RESP> encoder(Encoder<RESP> encoder) {
            this.encoder = encoder;
            return this;
        }

        public ContextBuilder<REQ, RESP> channelHandler(ChannelHandler channelInHandler) {
            this.channelInHandler = channelInHandler;
            return this;
        }


        public ContextBuilder<REQ, RESP> serializer(Serializer serializer) {
            this.serializer = serializer;
            return this;
        }

        public ContextBuilder<REQ, RESP> executorService(ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }

        public void init() {
            Assert.assertNotNull("decoder must be not null", decoder);
            Assert.assertNotNull("encoder ", encoder);
            Assert.assertNotNull("serializer ", serializer);
            Assert.assertNotNull("executorService ", executorService);
            Assert.assertNotNull("channelInHandler ", channelInHandler);
            Context<REQ, RESP> context = new Context<>(decoder, encoder, serializer, executorService,channelInHandler);
            logger.debug("init context success ------ \n Context=[{}]", context);
            ContextHolder.registed(context);
            logger.debug("registed context to ContextHolder success --\n Context=[{}]", context);
        }


    }


    public Decoder<REQ> getDecoder() {
        return decoder;
    }


    public Encoder<RESP> getEncoder() {
        return encoder;
    }


    public ChannelHandler getChannelHandler() {
        return channelInHandler;
    }


    public Serializer getSerializer() {
        return serializer;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public String toString() {
        return "Context{" +
                "decoder=" + decoder +
                ", encoder=" + encoder +
                ", serializer=" + serializer +
                ", executorService=" + executorService +
                '}';
    }
}
