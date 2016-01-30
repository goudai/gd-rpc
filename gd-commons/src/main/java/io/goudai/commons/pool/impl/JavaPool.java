package io.goudai.commons.pool.impl;

import io.goudai.commons.pool.Pool;
import io.goudai.commons.pool.PoolConfig;
import io.goudai.commons.pool.factory.ObjectFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by freeman on 2016/1/17.
 */
public class JavaPool<T> implements Pool<T> {
    private final PoolConfig poolConfig;
    private final ObjectFactory<T> factory;
    private final AtomicInteger useCount = new AtomicInteger(0);
    private final BlockingQueue<T> pool;


    public JavaPool(ObjectFactory<T> factory, PoolConfig poolConfig) {
        this.poolConfig = poolConfig;
        this.factory = factory;
        this.pool = new ArrayBlockingQueue<>(poolConfig.getMax());
        for (int i = 0; i < poolConfig.getInit(); i++) {
            this.pool.offer(factory.create());
        }

    }

    @Override
    public T borrowObject() throws InterruptedException {
        T t = null;
        if (useCount.get() >= this.poolConfig.getMax()) {
            t = pool.take();
        } else {
            if ((t = pool.poll()) == null) t = factory.create();
        }
        useCount.incrementAndGet();
        return t;
    }

    @Override
    public void returnObject(T t) {
        this.pool.offer(t);
        useCount.decrementAndGet();
    }
}
