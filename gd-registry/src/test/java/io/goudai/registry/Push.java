package io.goudai.registry;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;

/**
 * Created by Administrator on 2016/3/13.
 */
public class Push {
    public static void main(String[] args) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.builder().retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 3000)).connectionTimeoutMs(1000).namespace("gdRPC")
                .connectString("localhost:2181").build();
        client.start();
        String path = "myApp.1.0.0.goudai/io.goudai.cluster.domain.UserService/provider";
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
    }
}
