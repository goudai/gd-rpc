package io.goudai.commons.pool;

/**
 * Created by freeman on 2016/1/17.
 */
public interface Pool<T> {

    T borrowObject() throws Exception;

    void returnObject(T t);

}
