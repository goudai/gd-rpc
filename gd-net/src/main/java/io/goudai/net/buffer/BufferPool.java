package io.goudai.net.buffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * freeman
 */
public class BufferPool {
    private static Logger log = LoggerFactory.getLogger(BufferPool.class);// 日志记录器
    //TODO 暂时写死
    private static int maxBufferPoolSize = 1000;// 默认的直接缓冲区池上限大小1000
    private static int minBufferPoolSize = 1000;// 默认的直接缓冲区池下限大小1000
    private static int writeBufferSize = 8;// 响应缓冲区大小默认为8k

    private static BufferPool bufferPool = new BufferPool();// BufferPool的单实例

    private AtomicInteger usableCount = new AtomicInteger();// 可用缓冲区的数量
    private AtomicInteger createCount = new AtomicInteger();// 已创建了缓冲区的数量
    private ConcurrentLinkedQueue<ByteBuffer> queue = new ConcurrentLinkedQueue<ByteBuffer>();// 保存直接缓存的队列



    private BufferPool() {
        // 预先创建直接缓冲区
        for (int i = 0; i < minBufferPoolSize; ++i) {
            ByteBuffer bb = ByteBuffer.allocateDirect(writeBufferSize * 1024);
            this.queue.offer(bb);
        }

        // 设置可用的缓冲区和已创建的缓冲区数量
        this.usableCount.set(minBufferPoolSize);
        this.createCount.set(minBufferPoolSize);
    }


    public ByteBuffer allocate() {
        ByteBuffer bb = this.queue.poll();
        //TODO 目前无限制分配 只是在回收的时候舍弃掉多余的buffer
        if (bb == null) {// 如果缓冲区不够则创建新的缓冲区
            bb = ByteBuffer.allocate(writeBufferSize * 1024);
            this.createCount.incrementAndGet();
        } else {
            this.usableCount.decrementAndGet();
        }

        return bb;
    }


    public void releaseBuffer(ByteBuffer bb) {
        bb.clear();
        if (this.createCount.intValue() > maxBufferPoolSize && (this.usableCount.intValue() > (this.createCount.intValue() / 2))) {
            this.createCount.decrementAndGet();
        } else {
            this.queue.offer(bb);
            this.usableCount.incrementAndGet();
        }
    }

    public AtomicInteger getUsableCount() {
        return usableCount;
    }

    public AtomicInteger getCreateCount() {
        return createCount;
    }

    public static BufferPool getInstance() {
        return bufferPool;
    }
}