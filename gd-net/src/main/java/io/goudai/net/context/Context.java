package io.goudai.net.context;

import io.goudai.commons.util.Assert;
import io.goudai.net.handler.ChannelHandler;
import io.goudai.net.handler.codec.Decoder;
import io.goudai.net.handler.codec.Encoder;
import io.goudai.net.handler.serializer.Serializer;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * Created by freeman on 2016/1/12.
 */
@Builder
@Getter
@ToString
public class Context<REQ, RESP> {

    private final static Logger logger = LoggerFactory.getLogger(Context.class);

    private final Decoder<REQ> decoder;
    private final Encoder<RESP> encoder;
    private ChannelHandler channelHandler;
    private final Serializer serializer;
    private final ExecutorService executorService;
    public void init() {
        Assert.assertNotNull("decoder must be not null", decoder);
        Assert.assertNotNull("encoder ", encoder);
        Assert.assertNotNull("serializer ", serializer);
        Assert.assertNotNull("executorService ", executorService);
        Assert.assertNotNull("channelInHandler ", channelHandler);
        ContextHolder.registed(this);
        logger.debug("init context success !! \r\n{}", this);
    }





}
