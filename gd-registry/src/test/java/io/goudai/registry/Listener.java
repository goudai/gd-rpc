package io.goudai.registry;

import io.goudai.registry.protocol.URL;
import io.goudai.registry.zookeeper.Callback;
import io.goudai.registry.zookeeper.CallbackType;
import io.goudai.registry.zookeeper.PathChildrenCacheUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.RetryNTimes;

import java.util.List;

/**
 * Created by Administrator on 2016/3/13.
 */
public class Listener {

    public static void main(String[] args) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.builder().retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 3000)).connectionTimeoutMs(1000).namespace("gdRPC")
                .connectString("localhost:2181").build();
        client.start();
        String path = "/myApp.1.0.0.goudai/io.goudai.cluster.domain.UserService/provider";
//        client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
        PathChildrenCache pathChildrenCache = PathChildrenCacheUtil.pathChildrenCache(client, path, false, new Callback() {
            @Override
            public void notify(List<URL> urls, CallbackType type, Exception e) {

            }
        });
        pathChildrenCache.start();
        while (true) {
            Thread.sleep(Long.MAX_VALUE);
        }

    }
}
