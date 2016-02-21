package io.goudai.registry;

import io.goudai.registry.protocol.Protocol;

import java.util.List;

/**
 * Created by freeman on 2016/2/21.
 */

public interface Registry {
    /**
     *
     * @param interfaceClass 需要进行暴露的接口
     * @param service 暴露的接口的具体实现
     */
    void registry(Class<?> interfaceClass,Object service);

    /**
     *  获取当前协议的可以用服务集合
     * @param protocol
     * @return 可以服务的集合
     */
    List<Object> getService(Protocol protocol);




}
