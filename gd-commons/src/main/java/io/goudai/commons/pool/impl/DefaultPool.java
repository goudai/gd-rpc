package io.goudai.commons.pool.impl;

import io.goudai.commons.pool.Pool;
import io.goudai.commons.pool.PoolConfig;
import io.goudai.commons.pool.factory.ObjectFactory;

/**
 * Created by freeman on 2016/1/17.
 */
public class DefaultPool<T> implements Pool<T> {
    private PoolConfig poolConfig;
    private ObjectFactory<T> factory;

    public DefaultPool(ObjectFactory<T> factory,PoolConfig poolConfig) {
        this.poolConfig = poolConfig;
        this.factory = factory;
    }
    //TODO 等待具体实现
    @Override
    public T borrowObject()   {
        return null;
    }

    @Override
    public void returnObject(T t)   {

    }
}
