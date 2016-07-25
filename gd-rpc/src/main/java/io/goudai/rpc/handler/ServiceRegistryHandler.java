package io.goudai.rpc.handler;

/**
 * Created by freeman on 2016/1/28.
 */
public interface ServiceRegistryHandler {

    void registry(Class<?> interClass, Object service);
}
