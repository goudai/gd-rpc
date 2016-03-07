package io.goudai.registry;

import io.goudai.registry.protocol.Protocol;
import io.goudai.registry.protocol.URL;
import io.goudai.registry.zookeeper.Callback;
import io.goudai.registry.zookeeper.CallbackType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;

import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by freeman on 2016/2/21.
 */
@Getter
@Slf4j
public class ZooKeeRegistry implements Registry {

    final String root = "gdRPC";
    private CuratorFramework client;
    private String zkAddress;
    private int timeout;

    private Map<String, PathChildrenCache> pathChildrenCacheMap = new ConcurrentHashMap<>();

    public ZooKeeRegistry() {
        this("127.0.0.1", 2181);
    }

    public ZooKeeRegistry(String zkAddress, int timeout) {
        this.zkAddress = zkAddress;
        this.timeout = timeout;
        client = CuratorFrameworkFactory.builder()
                .retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 3000))
                .connectionTimeoutMs(timeout)
                .namespace(root)
                .connectString(zkAddress)
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
    public void unRegister(Protocol protocol) {
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
                cache.getListenable().addListener(new PathChildrenCacheListener(cache, path, callback));
                cache.start();
                pathChildrenCacheMap.put(path, cache);
            }
        } catch (Exception e) {
            throw new RuntimeException("subscribe service fail！service=[" + protocol.getService() + "]", e);
        }
    }

    @Override
    public void unSubscribe(Protocol protocol) {
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

    @Override
    public void startup() {
//        this.client.start();
    }

    @Override
    public void shutdown() {
        this.client.close();
    }


    public static class PathChildrenCacheListener implements org.apache.curator.framework.recipes.cache.PathChildrenCacheListener {

        private Callback callback;

        private PathChildrenCache pathChildrenCache;

        private String path;

        public PathChildrenCacheListener(PathChildrenCache pathChildrenCache, String path, Callback callback) {
            super();
            this.pathChildrenCache = pathChildrenCache;
            this.path = path;
            this.callback = callback;
        }

        @Override
        public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
            List<URL> urls = new ArrayList<>();
            try {
                PathChildrenCacheEvent.Type eventType = event.getType();
                switch (eventType) {
                    case CONNECTION_RECONNECTED:
                        pathChildrenCache.rebuild();
                        break;
                    case CONNECTION_SUSPENDED:
                    case CONNECTION_LOST:
                        log.warn("Connection error,waiting...");
                        break;
                    case CHILD_ADDED:
                        call(client, urls, CallbackType.CHILD_ADDED);
                        break;
                    case CHILD_UPDATED:
                        call(client, urls, CallbackType.CHILD_UPDATED);
                        break;
                    case CHILD_REMOVED:
                        call(client, urls, CallbackType.CHILD_REMOVED);
                        break;
                }
            } catch (Exception e) {
                callback.notify(urls, null, e);
            }
        }

        private void call(CuratorFramework client, List<URL> urls, CallbackType type) throws Exception {
            client.getChildren().forPath(path).forEach(url -> urls.add(URL.valueOf(url)));
            callback.notify(urls, type, null);
        }
    }

}
