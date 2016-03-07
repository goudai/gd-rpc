package io.goudai.cluster.factory;

import io.goudai.cluster.util.MethodUtil;
import io.goudai.net.Connector;
import io.goudai.net.session.factory.SessionFactory;
import io.goudai.registry.Registry;
import io.goudai.registry.protocol.Protocol;
import io.goudai.registry.protocol.URL;
import io.goudai.rpc.invoker.RequestSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.net.InetSocketAddress;

import static io.goudai.cluster.config.ClusterConfig.*;

@Slf4j
@RequiredArgsConstructor
public class KeyedPooledObjectFactory extends BaseKeyedPooledObjectFactory<URL, RequestSession> {

    private final Connector connector;
    private final SessionFactory sessionFactory;
    private final Registry registry;


    @Override
    public RequestSession create(URL key) throws Exception {
        RequestSession session = new RequestSession(connector, new InetSocketAddress(key.getHost(), Integer.parseInt(key.getPort())), sessionFactory);
        InetSocketAddress localAddress = (InetSocketAddress) session.getSession().getSocketChannel().getLocalAddress();
        //注册服务到注册中心
        Protocol protocol = Protocol
                .builder()
                .application(application)
                .version(version)
                .group(group)
                .host(host)
                .port(String.valueOf(localAddress.getPort()))
                .service(key.getService())
                .timeout(timeout)
                .type(CONSUMER)
                .methods(MethodUtil.getMethods(key.getService()))
                .build();
        registry.register(protocol);
        log.info("register consumer to registry --> {}",protocol);
        return session;
    }

    @Override
    public PooledObject<RequestSession> wrap(RequestSession value) {
        return new DefaultPooledObject<>(value);
    }

}