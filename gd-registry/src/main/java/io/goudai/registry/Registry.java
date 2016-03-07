package io.goudai.registry;

import io.goudai.commons.life.LifeCycle;
import io.goudai.registry.protocol.Protocol;
import io.goudai.registry.protocol.URL;
import io.goudai.registry.zookeeper.Callback;

import java.util.List;

/**
 * Created by freeman on 2016/2/21.
 */

public interface Registry extends LifeCycle {
	/**
	 * @param protocol
	 *            暴露的协议
	 */
	void register(Protocol protocol);

	/**
	 *
	 * @param protocol
	 */
	void unRegister(Protocol protocol);

	/**
	 *
	 * @param protocol
	 * @param callback
	 */
	void subscribe(Protocol protocol, Callback callback);

	/**
	 *
	 * @param protocol
	 */
	void unSubscribe(Protocol protocol);

	/**
	 * @param protocol
	 * @return //
	 *         "provider://host:port/com.goudai.test.UserService?timeout=1000&methods=test,getUser,findUser&app=gd-app&version=v1.0.0&group=gd-group"
	 */
	List<URL> lookup(Protocol protocol);

}
