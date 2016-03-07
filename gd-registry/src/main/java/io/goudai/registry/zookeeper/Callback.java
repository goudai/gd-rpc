package io.goudai.registry.zookeeper;

import io.goudai.registry.protocol.Protocol;

import java.util.List;

/**
 * Created by freeman on 2016/2/24.
 */
public interface Callback {

	void notify(List<Protocol> protocols, Exception e);
}
