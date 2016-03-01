package io.goudai.registry;

import io.goudai.registry.protocol.Protocol;
import io.goudai.registry.protocol.URL;
import io.goudai.registry.zookeeper.Callback;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by freeman on 2016/2/21.
 */
@Getter
@Slf4j
public class ZooKeeRegistry implements Registry {

    final String root = "gdRPC";
    private final String appConfigPathRoot = "appConfigPathRoot";
    private final CuratorFramework client;
    private String zkAddress;
    private int timeout;



    public ZooKeeRegistry(String zkAddress, int timeout) throws Exception {
        this.zkAddress = zkAddress;
        this.timeout = timeout;
        client = CuratorFrameworkFactory.builder()
                .retryPolicy(new RetryNTimes(1, 1))
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
        String path = "/" + protocol.getApp() + "." + protocol.getVersion() + "." + protocol.getGroup();
        if (this.client.checkExists().forPath(path) == null) {
            client.create().forPath(path);
        }
        //check service
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

    }

    @Override
    public void subscribe(Protocol protocol,Callback callback) {

    }

    @Override
    public void unSubscribe(Protocol protocol) {

    }

    @Override
    public List<URL> lookup(Protocol protocol) {
        List<URL> result = new ArrayList<>();
        try {
            String path = check(protocol, "provider");
            Set<String> strings = new HashSet<>(this.client.getChildren().forPath(path));
            strings.forEach(s -> result.add(URL.valueOf(s)));
        } catch (Exception e) {
            log.error(e.getMessage(),e);
//            log.warn("The available service is not found, please check the registration center or service provider. {}", protocol);
        }
        return result;
    }


}
