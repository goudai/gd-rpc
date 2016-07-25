package io.goudai.cluster.factory;

import io.goudai.cluster.util.MethodUtil;
import io.goudai.net.Connector;
import io.goudai.net.session.factory.SessionFactory;
import io.goudai.registry.Registry;
import io.goudai.registry.protocol.Protocol;
import io.goudai.registry.protocol.URL;
import io.goudai.rpc.invoker.RequestSession;
import io.goudai.rpc.invoker.RequestSessionFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.PooledObject;

import java.net.InetSocketAddress;

import static io.goudai.cluster.config.ClusterConfig.*;

/**
 * Created by freeman on 2016/3/13.
 */
@Slf4j
public class ClusterRequestSessionFactory
        extends RequestSessionFactory {
    private URL url;
    private Registry registry;

    public ClusterRequestSessionFactory(URL url, Registry registry, Connector connector, SessionFactory sessionFactory) {
        super(url.getHost(), url.getPort(), connector, sessionFactory);
        this.url = url;
        this.registry = registry;
    }

    @Override
    public RequestSession create() throws Exception {
        RequestSession session = super.create();
        InetSocketAddress localAddress = (InetSocketAddress) session.getSession().getSocketChannel().getLocalAddress();
        //注册服务到注册中心
        Protocol protocol = Protocol
                .builder()
                .application(application)
                .version(version)
                .group(group)
                .host(host)
                .port(localAddress.getPort())
                .service(url.getService())
                .timeout(timeout)
                .type(CONSUMER)
                .methods(MethodUtil.getMethods(url.getService()))
                .build();
        registry.register(protocol);
        log.info("register consumer to registry --> {}", protocol);
        return session;
    }

    @Override
    public void destroyObject(PooledObject<RequestSession> p) throws Exception {
        RequestSession session = p.getObject();
        InetSocketAddress localAddress = (InetSocketAddress) session.getSession().getSocketChannel().getLocalAddress();
        //注册服务到注册中心
        Protocol protocol = Protocol
                .builder()
                .application(application)
                .version(version)
                .group(group)
                .host(host)
                .port(localAddress.getPort())
                .service(url.getService())
                .timeout(timeout)
                .type(CONSUMER)
                .methods(MethodUtil.getMethods(url.getService()))
                .build();
        registry.register(protocol);
        log.info("unRegister consumer to registry --> {}", protocol);
        super.destroyObject(p);
    }
}
