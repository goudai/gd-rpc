package io.goudai.cluster.invoker;

import io.goudai.cluster.balance.Balance;
import io.goudai.cluster.util.MethodUtil;
import io.goudai.net.Connector;
import io.goudai.net.session.factory.DefaultSessionFactory;
import io.goudai.net.session.factory.SessionFactory;
import io.goudai.registry.Registry;
import io.goudai.registry.protocol.Protocol;
import io.goudai.registry.protocol.URL;
import io.goudai.registry.zookeeper.CallbackType;
import io.goudai.rpc.exception.RpcException;
import io.goudai.rpc.invoker.Invoker;
import io.goudai.rpc.invoker.RequestSession;
import io.goudai.rpc.model.Request;
import io.goudai.rpc.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static io.goudai.cluster.config.ClusterConfig.*;

/**
 * Created by freeman on 2016/3/4.
 */
@Slf4j
public class KeyPooledClusterInvoker implements Invoker {

    private final Registry registry;
    private final Balance balance;
    //存放可用的主机列表 将会在notify方法进行更新维护操作
    private final ConcurrentHashMap<String, List<URL>> availableHostCache = new ConcurrentHashMap<>();
    private final Connector connector;
    private final SessionFactory sessionFactory = new DefaultSessionFactory();
    /**
     * URL@6B1635E11--> [RequestSession@6e06451e,RequestSession@6e05351e]
     */
    private final GenericKeyedObjectPool<URL, RequestSession> availableSessionCache;


    public KeyPooledClusterInvoker(Registry registry, Balance balance, Connector connector) {
        this.registry = registry;
        this.balance = balance;
        this.connector = connector;
        GenericKeyedObjectPoolConfig genericKeyedObjectPoolConfig = new GenericKeyedObjectPoolConfig();
        genericKeyedObjectPoolConfig.setMaxTotal(500);
        availableSessionCache = new GenericKeyedObjectPool<>(new BaseKeyedPooledObjectFactory<URL, RequestSession>() {
            @Override
            public RequestSession create(URL url) throws Exception {
                RequestSession session = new RequestSession(connector, new InetSocketAddress(url.getHost(), url.getPort()), sessionFactory);
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
            public PooledObject<RequestSession> wrap(RequestSession value) {
                return new DefaultPooledObject<>(value);
            }
        });

    }

    /**
     * 延迟获取
     *
     * @param request
     * @return
     * @throws RpcException
     */
    @Override
    public Response invoke(Request request) throws RpcException {
        List<URL> urls = this.lookupAvailableHost(request.getService());
        if (urls.isEmpty()) {
            throw new RpcException("The available service is not found, please check the registration center or service provider." +
                    "application -->" + application + " version -->" + version + " group -->" + group + " service -->" + request.getService());
        }
        final URL url = this.balance.select(urls);
        Response response = Response.builder().id(request.getId()).build();
        RequestSession requestSession = null;
        try {
            requestSession = this.availableSessionCache.borrowObject(url);
            response = requestSession.invoker(request);
        } catch (Exception e) {
            response.setException(e);
        } finally {
            this.availableSessionCache.returnObject(url, requestSession);
        }
        return response;
    }

    @Override
    public void shutdown() {

    }

    /*
    subscribe --> /gdRPC/pay.1.0.0.default/com.gd.service.UserService
    added
        local
            hostCache -> {"pay#1.0.0#default#com.gd.service.UserService":[10.100.1.1:6161,10.100.1.2:6161]}
            invokerCache ->{{10.100.1.1:6161:invoker@saE22D4},{10.100.1.2:6161:invoker@saE22D4}}
        remote
            zk -> provider
                    --10.100.1.1:6161/com.gd.service.UserService
                    --10.100.1.2:6161/com.gd.service.UserService
        change --------------||
            zk -> provider
                    --10.100.1.1:6161/com.gd.service.UserService
                    --10.100.1.2:6161/com.gd.service.UserService
                    --10.100.1.3:6161/com.gd.service.UserService
        local
            hostCache -> {"pay#1.0.0#default#com.gd.service.UserService":[10.100.1.1:6161,10.100.1.2:6161]} --> add 10.100.1.3:6161
            invokerCache ->{{10.100.1.1:6161:invoker@saE22D4},{10.100.1.2:6161:invoker@saE22D4}，{10.100.1.3:6161:invoker@saE22D4}} -->  add {10.100.1.3:6161:invoker@saE22D4}


     removed
        local
            hostCache -> {"pay#1.0.0#default#com.gd.service.UserService":[10.100.1.1:6161,10.100.1.2:6161,10.100.1.3:616]}
            invokerCache ->{{10.100.1.1:6161:invoker@saE22D4},{10.100.1.2:6161:invoker@saE22D4}{10.100.1.3:6161:invoker@saE22D4}}
        remote
            zk -> provider
                    --10.100.1.1:6161/com.gd.service.UserService
                    --10.100.1.2:6161/com.gd.service.UserService
                    --10.100.1.3:6161/com.gd.service.UserService
        change --------------||
            zk -> provider
                    --10.100.1.1:6161/com.gd.service.UserService
                    --10.100.1.2:6161/com.gd.service.UserService
        local
            hostCache -> {"pay#1.0.0#default#com.gd.service.UserService":[10.100.1.1:6161,10.100.1.2:6161]} --> delete 10.100.1.3:6161
            invokerCache ->{{10.100.1.1:6161:invoker@saE22D4},{10.100.1.2:6161:invoker@saE22D4}，{10.100.1.3:6161:invoker@saE22D4}}


     */
    private void subscribe(Protocol protocol) {
        this.registry.subscribe(protocol, (newUrls, type, e) -> {
            String localKey = getServiceKey(protocol.getService());
            synchronized (localKey) {
                log.info("notify -->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + newUrls);
                List<URL> urlList = availableHostCache.get(localKey);
             /*发现新的主机加入service provider*/
                if (CallbackType.CHILD_ADDED.equals(type)) {
                    for (URL url : newUrls) {
                        // new host add to host cache
                        if (!urlList.contains(url)) {
                            urlList.add(url);
                        }
                    }
                }
            /*发现有服务主机挂掉*/
                else if (CallbackType.CHILD_REMOVED.equals(type)) {
                    Iterator<URL> iterator = urlList.iterator();
                    while (iterator.hasNext()) {
                        URL next = iterator.next();
                        if (!newUrls.contains(next)) {
                            iterator.remove();
                            this.availableSessionCache.clear(next);
                        }
                    }
                }
            }
        });
    }

    /**
     * 从注册中心查找可用主机并缓存
     *
     * @param service
     * @return
     */
    private List<URL> lookupAvailableHost(String service) {
        String key = getServiceKey(service);
        List<URL> urls = new ArrayList<>();
        if (!availableHostCache.containsKey(key) || availableHostCache.get(key) == null || availableHostCache.get(key).isEmpty()) {
            Protocol protocol = Protocol
                    .builder()
                    .application(application)
                    .version(version)
                    .group(group)
                    .service(service)
                    .type("provider")
                    .build();
            urls = registry.lookup(protocol);
            if (urls.isEmpty()) return urls;
            availableHostCache.put(key, urls);
            URL url = urls.get(0);
            protocol.setHost(url.getHost());
            protocol.setPort(url.getPort());
            this.subscribe(protocol);
        } else return availableHostCache.get(key);
        return urls;
    }


    private String getServiceKey(String service) {
        return application + "#" + version + "#" + group + "#" + service;
    }


}
