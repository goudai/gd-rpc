package io.goudai.rpc.handler;

/**
 * Created by Administrator on 2016/1/28.
 */
public interface ServiceRegistryHandler {

    void registry(Class<?> interClass, Object service);
}
