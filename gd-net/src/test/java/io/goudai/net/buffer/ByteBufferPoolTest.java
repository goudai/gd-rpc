package io.goudai.net.buffer;

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2016/1/12.
 */

public class ByteBufferPoolTest {

    @Test
    public void testAllocateDirect() {
        byte[] bbs = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bbs.length + 4).putInt(bbs.length).put(bbs);
        byteBuffer.flip();
        int anInt = byteBuffer.getInt();
        Assert.assertEquals(bbs.length, anInt);
        byte[] bs = new byte[anInt];
        byteBuffer.get(bs);

    }

    @Test
    public void testBufferPool(){
        BufferPool instance = BufferPool.getInstance();
        ByteBuffer allocate = instance.allocate();
        int limit = allocate.limit();
        //TODO 此处写死先
        Assert.assertEquals(limit,8192);



        instance.releaseBuffer(allocate);


    }

}
