package io.goudai.commons.pool.factory;

/**
 * Created by freeman on 2016/1/17.
 */
public interface ObjectFactory<T> {

    T create();

    void destroy(T object);
}
