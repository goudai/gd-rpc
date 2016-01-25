package io.goudai.net.session;

import io.goudai.net.buffer.IoBuffer;
import io.goudai.net.session.exception.ConnectedTimeoutException;
import lombok.Data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by freeman on 2016/1/11.
 */
@Data
public abstract class AbstractSession {
    protected String id;
    /*具体的server 于 client之间建立的真实通道*/
    protected SocketChannel socketChannel;
    protected SelectionKey key;
    /* 数据读物的Buffer，用于保存半包数据，新数据将在在半包后继续填充直到一个完整的packet*/
    protected IoBuffer readBuffer;
    /* Byffer队列 在调用write方法时候实际写入改队列，writeEvent触发的时候写入正在的channel中*/
    protected Queue<ByteBuffer> writeBufferQueue = new ConcurrentLinkedQueue<>();
    /** session的创建时间*/
    protected Date createdTime;
    /* 每次session没操作更新 */
    protected Date updateTime;
    private CountDownLatch connectLatch = new CountDownLatch(1);


    public AbstractSession(SocketChannel socketChannel, SelectionKey key) {
        this.socketChannel = socketChannel;
        this.key = key;
        this.createdTime = new Date();
    }


    public void finishConnect(){
        this.connectLatch.countDown();
    }

    public boolean await(long miss){
        try {
            return this.connectLatch.await(miss, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // ig
        }
        throw new ConnectedTimeoutException("connected server timeout ");
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
    public abstract void realWrite() throws IOException;

    /**
     * 写入数据
     *
     * @param
     * @throws IOException
     */
    public abstract   void write(Object t);






}
