package io.goudai.rpc.bootstarp;

import io.goudai.commons.LifeCycle;

/**
 * Created by Administrator on 2016/3/13.
 */
public interface Bootstrap extends LifeCycle {
    <T> T getService(Class<T> clazz);

    @Override
    void startup();

    @Override
    void shutdown();
}
