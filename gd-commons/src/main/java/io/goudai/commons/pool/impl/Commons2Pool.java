package io.goudai.commons.pool.impl;

import io.goudai.commons.pool.Pool;
import io.goudai.commons.pool.factory.ObjectFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Created by freeman on 2016/1/30.
 */
public class Commons2Pool<T> implements Pool<T> {
    private GenericObjectPool<T> pool;
    private GenericObjectPoolConfig config;
    private ObjectFactory<T> factory;

    public Commons2Pool(ObjectFactory<T> factory) {
        this.factory = factory;
        config = new GenericObjectPoolConfig();
        config.setMaxTotal(100);
        config.setMaxIdle(100);
        this.pool = new GenericObjectPool<T>(new BasePooledObjectFactory<T>() {
            @Override
            public T create() throws Exception {
                return  factory.create();
            }

            @Override
            public void destroyObject(PooledObject<T> p) throws Exception {
                factory.destroy(p.getObject());
            }

            @Override
            public PooledObject<T> wrap(T t) {
                return new DefaultPooledObject(t);
            }
        },config);
    }

    public Commons2Pool(GenericObjectPool<T> pool, GenericObjectPoolConfig config,ObjectFactory<T> factory) {
        this.factory = factory;
        this.pool = pool;
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
}
