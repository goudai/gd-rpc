package io.goudai.net.session;

import io.goudai.net.buffer.BufferPool;
import io.goudai.net.buffer.IoBuffer;
import io.goudai.net.context.Context;
import io.goudai.net.context.ContextHolder;
import io.goudai.net.handler.ChannelHandler;
import io.goudai.net.handler.codec.Decoder;
import io.goudai.net.handler.codec.Encoder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by freeman on 2016/1/8.
 */

public class Session<REQ, RESP> extends AbstractSession {


    private final Decoder<REQ> decoder;
    private final ChannelHandler channelHandler;
    private final Encoder<RESP> encoder;
    private final ExecutorService executorService;

    public Session(SocketChannel socketChannel, SelectionKey key) {
        super(socketChannel, key);
        Context<REQ, RESP> context = ContextHolder.getContext();
        this.decoder = context.getDecoder();
        this.channelHandler = context.getChannelHandler();
        this.encoder = context.getEncoder();
        this.executorService = context.getExecutorService();
    }

    @Override
    public void read() throws IOException {
        if (readBuffer == null) readBuffer = IoBuffer.allocate(1024 * 8);
        ByteBuffer buf = BufferPool.getInstance().allocate();
        try {
            //TODO 考虑是否每次强行读完 还是选择读物一个最大包
            int n = 0;
            while ((n = socketChannel.read(buf)) > 0) {
                if (n < 0) this.close();
                buf.flip();
                byte[] bytes = new byte[buf.limit()];
                buf.get(bytes);
                readBuffer.writeBytes(bytes, 0, buf.limit());
                buf.clear();
            }
        } finally {
            BufferPool.getInstance().releaseBuffer(buf);
        }
        IoBuffer tempBuf = readBuffer.flip();
        List<REQ> result = new ArrayList<>();
        IoBuffer in = decoder.decode(tempBuf, result);
        this.restReadBuffer(in);
        result.forEach(r -> this.executorService.execute(() -> {
            ContextHolder.getContext().getSessionListener().onRead(this, r);
            channelHandler.received(this, r);
        }));

    }

    @Override
    public void realWrite() throws Exception {
        while (true) {
            ByteBuffer buffer = writeBufferQueue.peek();
            if (buffer == null) {
                //通道的事件写完之后取消写事件
                key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
                return;
            }
            int write = socketChannel.write(buffer);
            if (write < 0) this.close();
            if (write == 0 && buffer.remaining() > 0) {
                return;
            }

            if (buffer.remaining() != 0) {
                return;
            }

            writeBufferQueue.remove();
        }
    }

    @Override
    public void write(Object object) {
        ContextHolder.getContext().getSessionListener().onWrite(this, object);
        this.writeBufferQueue.offer(encoder.encode((RESP) object));
        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        key.selector().wakeup();
    }

    private void restReadBuffer(IoBuffer tempBuf) {
        if (tempBuf != null && tempBuf.remaining() > 0) {
            readBuffer = IoBuffer.wrap(tempBuf.array());
        } else {
            readBuffer = null;
        }
    }


}
