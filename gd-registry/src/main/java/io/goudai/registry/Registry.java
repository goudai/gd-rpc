package io.goudai.registry;

import io.goudai.registry.protocol.Protocol;
import io.goudai.registry.protocol.URL;

import java.util.List;

/**
 * Created by freeman on 2016/2/21.
 */

public interface Registry {
    /**
     * @param protocol  暴露的协议
     */
    void registry(Protocol protocol);

    /**
     *
     * @param protocol
     * @return //"provider://host:port/com.goudai.test.UserService?timeout=1000&methods=test,getUser,findUser&app=gd-app&version=v1.0.0&group=gd-group"
     */
    List<URL> lookup(Protocol protocol);


}
