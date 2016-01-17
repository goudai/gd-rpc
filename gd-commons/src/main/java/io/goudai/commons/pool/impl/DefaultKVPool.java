package io.goudai.commons.pool.impl;

import io.goudai.commons.pool.KVPool;
import io.goudai.commons.pool.PoolConfig;
import io.goudai.commons.pool.factory.KVObjectFactory;

/**
 * Created by freeman on 2016/1/17.
 */
public class DefaultKVPool<K,V> implements KVPool<K,V> {

    private PoolConfig poolConfig;
    /*解耦对象的具体实例化*/
    private KVObjectFactory<K,V> factory;

    public DefaultKVPool(KVObjectFactory<K,V> factory,PoolConfig poolConfig) {
        this.poolConfig = poolConfig;
        this.factory = factory;
    }
    //TODO 需要实习KV缓存



    @Override
    public V borrowObject(K k) throws Exception {
        return null;
    }

    @Override
    public void returnObject(K k, V v) throws Exception {

    }
}
