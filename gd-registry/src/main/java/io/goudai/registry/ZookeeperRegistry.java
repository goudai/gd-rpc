package io.goudai.registry;

import io.goudai.registry.protocol.Protocol;

import java.util.List;

/**
 * Created by freeman on 2016/2/21.
 * //TODO
 */
public class ZookeeperRegistry implements Registry {

    @Override
    public void registry(Class<?> interfaceClass, Object service) {

    }

    @Override
    public List<Object> getService(Protocol protocol) {
        return null;
    }
}
