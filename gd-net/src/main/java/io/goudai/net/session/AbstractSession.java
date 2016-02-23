package io.goudai.net.session;

import io.goudai.net.buffer.IoBuffer;
import io.goudai.net.context.ContextHolder;
import io.goudai.net.session.exception.ConnectedTimeoutException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by freeman on 2016/1/11.
 */
@Getter
@Setter
@Slf4j
public abstract class AbstractSession  {
    /*session的唯一标示*/

    protected final long id;
    /*session的创建时间*/
    protected final long createdTime;
    /*具体的server 于 client之间建立的真实通道*/

    protected final SocketChannel socketChannel;
    /* session最后一次进行读写的时间 */
    protected long updateTime;
    protected SelectionKey key;
    /* 数据读物的Buffer，用于保存半包数据，新数据将在在半包后继续填充直到一个完整的packet*/
    protected IoBuffer readBuffer;
    /* Byffer队列 在调用write方法时候实际写入改队列，writeEvent触发的时候写入正在的channel中*/
    protected Queue<ByteBuffer> writeBufferQueue = new ConcurrentLinkedQueue<>();
    private Status status;
    private CountDownLatch connectLatch = new CountDownLatch(1);

    public AbstractSession(SocketChannel socketChannel, SelectionKey key) {
        this.createdTime = System.currentTimeMillis();
        this.id = SessionIdGenerator.getId();
        this.socketChannel = socketChannel;
        this.key = key;
        this.status = Status.NEW;
    }

    public void finishConnect() {
        this.connectLatch.countDown();
    }

    public void awaitConnected(long time) {
        try {
             if(!this.connectLatch.await(time, TimeUnit.MILLISECONDS)){
                 throw new ConnectedTimeoutException("connected server timeout ! timeout=[" + time + "]");
             };
        } catch (InterruptedException e) {
            // ig
        }
    }


    public void close()   {
        synchronized (this) {
            if (isClosed()) return;
            ContextHolder.getContext().getSessionListener().onDestory(this);
            try {
                if (key != null) key.cancel();
            } catch (Exception e) {
                log.warn(e.getMessage(),e);
            }
            try {
                if (this.socketChannel != null) this.socketChannel.close();
            } catch (IOException e) {
                log.warn(e.getMessage(),e);
            }
        }
    }

    public boolean isClosed() {
        return this.status == Status.CLOSED;
    }

    public void updateTime() {
        this.updateTime = System.currentTimeMillis();
    }

    /**
     * 处理socketChannel的读事件
     *
     * @throws IOException
     */
    public abstract void read() throws IOException;

    /**
     * 真实的写如socketChannel通道
     *
     * @throws IOException
     */
    public abstract void realWrite() throws Exception;

    /**
     * 写入数据
     *
     * @param
     * @throws IOException
     */
    public abstract void write(Object t);

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AbstractSession{");
        sb.append("id=").append(id);
        sb.append(", createdTime=").append(new Date(createdTime).toLocaleString());
        sb.append(", status=").append(status);
        try {
            if(socketChannel.isOpen())
            sb.append(",socketChannel=").append(socketChannel.getLocalAddress() +" --> " + socketChannel.getRemoteAddress());
        } catch (IOException e) {
           log.warn(e.getMessage(),e);
        }
        sb.append('}');
        return sb.toString();
    }

    public static enum Status {
        NEW,//表示session刚刚创建
        CONNECTED,
        OPEN,//标示session已经注册到了reactor
        CLOSED, //标示session已经关闭
        ERROR
    }
}

class SessionIdGenerator {
    private final static AtomicLong sessionId = new AtomicLong(0);

    static long getId() {
        return sessionId.incrementAndGet();
    }
}