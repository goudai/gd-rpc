package io.goudai.registry;

import io.goudai.registry.protocol.Protocol;
import io.goudai.registry.protocol.URL;
import io.goudai.registry.zookeeper.Callback;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;

/**
 * Created by freeman on 2016/2/21.
 */
@Getter
@Slf4j
public class ZooKeeRegistry implements Registry {

	final String root = "gdRPC";
	private final String appConfigPathRoot = "appConfigPathRoot";
	private CuratorFramework client;
	private String zkAddress;
	private int timeout;

	private Map<String, PathChildrenCache> pathChildrenCacheMap = new ConcurrentHashMap<>();

	public ZooKeeRegistry() throws Exception {
		this("127.0.0.1", 2181);
	}

	public ZooKeeRegistry(String zkAddress, int timeout) throws Exception {
		this.zkAddress = zkAddress;
		this.timeout = timeout;
		client = CuratorFrameworkFactory.builder().retryPolicy(new RetryNTimes(1, 1)).connectionTimeoutMs(timeout).namespace(root).connectString(zkAddress)
				.build();
		this.client.start();

	}

	@Override
	public void register(Protocol protocol) {
		try {
			String path = check(protocol, "provider") + "/" + URLEncoder.encode(protocol.value(), "utf-8");
			if (this.client.checkExists().forPath(path) == null) {
				this.client.create().withMode(CreateMode.EPHEMERAL).forPath(path);
			}
		} catch (Exception e) {
			throw new RuntimeException("registry service fail！service=[" + protocol.getService() + "]", e);
		}
	}

	private String check(Protocol protocol, String type) throws Exception {
		String path = "/" + protocol.getApplication() + "." + protocol.getVersion() + "." + protocol.getGroup();
		if (this.client.checkExists().forPath(path) == null) {
			client.create().forPath(path);
		}
		// check service
		path = path + "/" + protocol.getService();
		if (this.client.checkExists().forPath(path) == null) {
			client.create().forPath(path);
		}
		path = path + "/" + type;
		if (this.client.checkExists().forPath(path) == null) {
			client.create().forPath(path);
		}
		return path;
	}

	@Override
	public void unregister(Protocol protocol) {
		try {
			String path = check(protocol, "provider") + "/" + URLEncoder.encode(protocol.value(), "utf-8");
			if (this.client.checkExists().forPath(path) == null) {
				this.client.delete().forPath(path);
			}
		} catch (Exception e) {
			throw new RuntimeException("unregistry service fail！service=[" + protocol.getService() + "]", e);
		}
	}

	@Override
	public void subscribe(Protocol protocol, Callback callback) {
		try {
			String path = check(protocol, "provider");
			PathChildrenCache cache = pathChildrenCacheMap.get(path);
			if (cache == null) {
				cache = new PathChildrenCache(client, path, false);
				cache.start();
				cache.getListenable().addListener(new PathChildrenCacheListener(cache, callback));
				pathChildrenCacheMap.put(path, cache);
			}
		} catch (Exception e) {
			throw new RuntimeException("subscribe service fail！service=[" + protocol.getService() + "]", e);
		}
	}

	@Override
	public void unsubscribe(Protocol protocol) {
		try {
			String path = check(protocol, "provider");
			PathChildrenCache cache = pathChildrenCacheMap.get(path);
			if (cache != null) {
				CloseableUtils.closeQuietly(cache);
			}
		} catch (Exception e) {
			throw new RuntimeException("unsubscribe service fail！service=[" + protocol.getService() + "]", e);
		}
	}

	@Override
	public List<URL> lookup(Protocol protocol) {
		List<URL> result = new ArrayList<>();
		try {
			String path = check(protocol, "provider");
			Set<String> strings = new HashSet<>(this.client.getChildren().forPath(path));
			strings.forEach(s -> result.add(URL.valueOf(s)));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			// log.warn("The available service is not found, please check the registration center or service provider. {}",
			// protocol);
		}
		return result;
	}

	public static class PathChildrenCacheListener implements org.apache.curator.framework.recipes.cache.PathChildrenCacheListener {

		private Callback callback;

		private PathChildrenCache pathChildrenCache;

		public PathChildrenCacheListener(PathChildrenCache pathChildrenCache, Callback callback) {
			super();
			this.pathChildrenCache = pathChildrenCache;
			this.callback = callback;
		}

		@Override
		public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
			List<Protocol> protocols = new ArrayList<>();
			try {
				pathChildrenCache.getCurrentData().forEach(v -> protocols.add(Protocol.valueOf(v.getPath())));
				callback.notify(protocols, null);
			} catch (Exception e) {
				callback.notify(protocols, e);
			}
		}
	}

}
