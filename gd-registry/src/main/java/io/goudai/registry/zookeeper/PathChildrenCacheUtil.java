package io.goudai.registry.zookeeper;

import io.goudai.registry.protocol.URL;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PathChildrenCacheUtil {
    public static PathChildrenCache pathChildrenCache(CuratorFramework client, String path, Boolean cacheData, Callback callback) throws Exception {
        final PathChildrenCache cached = new PathChildrenCache(client, path, cacheData);
        cached.getListenable().addListener((_client, event) -> {
            List<URL> urls = new ArrayList<>();
            try {
                PathChildrenCacheEvent.Type eventType = event.getType();
                switch (eventType) {
                    case CONNECTION_RECONNECTED:
                        log.info("Connection reconnected");
                        break;
                    case CONNECTION_SUSPENDED:
                    case CONNECTION_LOST:
                        log.warn("Connection error, waiting...");
                        break;
                    case CHILD_ADDED:
                        call(client.getChildren().forPath(path), urls, CallbackType.CHILD_ADDED, callback);
                        break;
                    case CHILD_UPDATED:
                        call(client.getChildren().forPath(path), urls, CallbackType.CHILD_UPDATED, callback);
                        break;
                    case CHILD_REMOVED:
                        call(client.getChildren().forPath(path), urls, CallbackType.CHILD_REMOVED, callback);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                callback.notify(urls, null, e);
            }
        });
        return cached;
    }

    private static void call(List<String> notifyPaths, List<URL> urls, CallbackType type, Callback callback) throws Exception {
        notifyPaths.forEach(v -> {
            try {
                String decode = URLDecoder.decode(v, "UTF-8");
                System.out.println(decode);
                urls.add(URL.valueOf(decode));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
        callback.notify(urls, type, null);
    }
}