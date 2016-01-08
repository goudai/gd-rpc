package io.goudai.session;

import io.goudai.buffer.IoBuffer;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by freeman on 2016/1/8.
 */
public class Session {
    protected String id;
    protected SocketChannel socketChannel;
    protected SelectionKey key;
    protected IoBuffer readBuffer;
    protected Queue<ByteBuffer> writeBufferQueue = new ConcurrentLinkedQueue<>();
    protected Date createdTime;
    protected Date updateTime;
    private CountDownLatch connectLatch = new CountDownLatch(1);

    public Session(SocketChannel socketChannel, SelectionKey key, Date createdTime) {
        this.socketChannel = socketChannel;
        this.key = key;
        this.createdTime = createdTime;
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
}
