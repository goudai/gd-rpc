package io.goudai.session;

import io.goudai.buffer.IoBuffer;
import io.goudai.session.exception.ConnectedTimeoutException;

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
public abstract class AbstractSession<T> {
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
    public abstract   void write(T t);



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public SelectionKey getKey() {
        return key;
    }

    public void setKey(SelectionKey key) {
        this.key = key;
    }

    public IoBuffer getReadBuffer() {
        return readBuffer;
    }

    public void setReadBuffer(IoBuffer readBuffer) {
        this.readBuffer = readBuffer;
    }

    public Queue<ByteBuffer> getWriteBufferQueue() {
        return writeBufferQueue;
    }

    public void setWriteBufferQueue(Queue<ByteBuffer> writeBufferQueue) {
        this.writeBufferQueue = writeBufferQueue;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public CountDownLatch getConnectLatch() {
        return connectLatch;
    }

    public void setConnectLatch(CountDownLatch connectLatch) {
        this.connectLatch = connectLatch;
    }


}
