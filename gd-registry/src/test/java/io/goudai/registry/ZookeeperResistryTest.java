package io.goudai.registry;

import io.goudai.commons.util.NetUtil;
import io.goudai.registry.protocol.Protocol;
import org.junit.Test;

/**
 * Created by Administrator on 2016/2/29.
 */
public class ZookeeperResistryTest {
    @Test
    public void testRegistry() throws Exception {
        ZooKeeRegistry zooKeeRegistry = new ZooKeeRegistry("127.0.0.1:2181", 3000);
        Protocol protocol = new Protocol();
        protocol.setType("provider");
        protocol.setApp("test-app");
        protocol.setHost(NetUtil.getLocalIp());
        protocol.setPort("6161");
        protocol.setService("com.test.service.UserService");
        zooKeeRegistry.registry(protocol);
        protocol.setPort("8888");
        zooKeeRegistry.registry(protocol);


        zooKeeRegistry.lookup(protocol).forEach(System.out::println);
    }


}
