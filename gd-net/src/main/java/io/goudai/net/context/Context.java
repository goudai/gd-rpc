package io.goudai.net.context;

import io.goudai.commons.util.Assert;
import io.goudai.net.handler.ChannelHandler;
import io.goudai.net.handler.codec.Decoder;
import io.goudai.net.handler.codec.Encoder;
import io.goudai.net.handler.serializer.Serializer;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;

/**
 * Created by freeman on 2016/1/12.
 */
@Builder
@Getter
@ToString
@Slf4j
public class Context<REQ, RESP> {


    protected final Decoder<REQ> decoder;
    protected final Encoder<RESP> encoder;
    protected final ChannelHandler channelHandler;
    protected final Serializer serializer;
    protected final ExecutorService executorService;

    public void init() {
        Assert.assertNotNull("decoder must be not null", decoder);
        Assert.assertNotNull("encoder ", encoder);
        Assert.assertNotNull("serializer ", serializer);
        Assert.assertNotNull("executorService ", executorService);
        Assert.assertNotNull("channelInHandler ", channelHandler);
        ContextHolder.registed(this);
        log.debug("init context success !! \r\n{}", this);
    }





}
