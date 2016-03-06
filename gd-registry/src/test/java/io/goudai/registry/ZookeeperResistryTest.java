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
        Registry zooKeeRegistry = new ZooKeeRegistry("127.0.0.1:2181", 3000);

        Protocol protocol = Protocol.builder()
                .type("provider")
                .application("test-app")
                .host(NetUtil.getLocalIp())
                .port("6161")
                .service("com.test.service.UserService").build();
        zooKeeRegistry.register(protocol);
        protocol.setPort("8888");
        zooKeeRegistry.register(protocol);


        zooKeeRegistry.lookup(protocol).forEach(System.out::println);
    }


}
