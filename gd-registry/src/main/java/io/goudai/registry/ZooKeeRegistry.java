package io.goudai.registry;

import io.goudai.registry.protocol.Protocol;
import io.goudai.registry.protocol.URL;
import io.goudai.registry.zookeeper.Callback;
import io.goudai.registry.zookeeper.PathChildrenCacheUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
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
        this("127.0.0.1:2181", 3000);
    }

    public ZooKeeRegistry(String zkAddress, int timeout) {
        this.zkAddress = zkAddress;
        this.timeout = timeout;
        client = CuratorFrameworkFactory.builder().retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 3000)).connectionTimeoutMs(timeout).namespace(root)
                .connectString(zkAddress).build();
        this.client.start();
    }

    @Override
    public void register(Protocol protocol) {
        try {
            String path = check(protocol, protocol.getType()) + "/" + URLEncoder.encode(protocol.value(), "utf-8");
            log.info("register path " + path);
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
            String path = check(protocol, protocol.getType()) + "/" + URLEncoder.encode(protocol.value(), "utf-8");
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
            String path = check(protocol, protocol.getType());
            synchronized (path) {
                PathChildrenCache cache = pathChildrenCacheMap.get(path);
                if (cache == null) {
                    log.info("");
                    cache = PathChildrenCacheUtil.pathChildrenCache(this.client, path, false, callback);
                    cache.start();
                    log.info("subscribe protocol =" + path);
                    pathChildrenCacheMap.put(path, cache);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("subscribe service fail！service=[" + protocol.getService() + "]", e);
        }
    }

    @Override
    public void unSubscribe(Protocol protocol) {
        try {
            String path = check(protocol, protocol.getType());
            PathChildrenCache removed = pathChildrenCacheMap.remove(path);
            if (removed != null) {
                CloseableUtils.closeQuietly(removed);
            }
        } catch (Exception e) {
            throw new RuntimeException("unsubscribe service fail！service=[" + protocol.getService() + "]", e);
        }
    }

    @Override
    public List<URL> lookup(Protocol protocol) {
        List<URL> result = new ArrayList<>();
        try {
            String path = check(protocol, protocol.getType());
            Set<String> strings = new HashSet<>(this.client.getChildren().forPath(path));
            strings.forEach(s -> result.add(URL.valueOf(s)));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
            // log.warn("The available service is not found, please check the registration center or service provider. {}",
            // protocol);
        }
        return result;
    }

    @Override
    public void startup() {
        // this.client.start();
    }

    @Override
    public void shutdown() {
        this.client.close();
    }


}
