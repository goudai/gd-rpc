package io.goudai.registry.zookeeper;

import io.goudai.registry.protocol.URL;

import java.util.List;

/**
 * Created by freeman on 2016/2/24.
 */
public interface Callback {

    void notify(List<URL> protocols, CallbackType type, Exception e);
}
