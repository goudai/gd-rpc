package io.goudai.commons.pool.impl;

import io.goudai.commons.pool.Pool;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Created by freeman on 2016/1/30.
 */
public class CommonsPool2Impl<T> implements Pool<T> {
    private GenericObjectPool<T> pool;
    private GenericObjectPoolConfig config;
    private PooledObjectFactory<T> factory;


    public CommonsPool2Impl(GenericObjectPoolConfig config, PooledObjectFactory<T> factory) {
        this.pool = new GenericObjectPool<T>(factory, config);
        this.factory = factory;
        this.config = config;
    }

    @Override
    public T borrowObject() throws Exception {
        return this.pool.borrowObject();
    }

    @Override
    public void returnObject(T o) {
        this.pool.returnObject(o);
    }

    @Override
    public void destroy() {
        this.pool.close();
    }

    public static void main(String[] args) {
        final CommonsPool2Impl<Object> objectCommonsPool2 = new CommonsPool2Impl<>(null,null);
    }
}
