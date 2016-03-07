package io.goudai.cluster.invoker;

import io.goudai.cluster.balance.Balance;
import io.goudai.registry.Registry;
import io.goudai.registry.protocol.Protocol;
import io.goudai.registry.protocol.URL;
import io.goudai.registry.zookeeper.Callback;
import io.goudai.registry.zookeeper.CallbackType;
import io.goudai.rpc.exception.RpcException;
import io.goudai.rpc.invoker.Invoker;
import io.goudai.rpc.invoker.RequestSession;
import io.goudai.rpc.model.Request;
import io.goudai.rpc.model.Response;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static io.goudai.cluster.config.ClusterConfig.*;

/**
 * Created by freeman on 2016/3/4.
 */
public class ClusterInvoker implements Invoker, Callback, Balance {

    private final Registry registry;
    /**
     * URL@6B1635E11--> [RequestSession@6e06451e,RequestSession@6e05351e]
     */
    private final GenericKeyedObjectPool<URL, RequestSession> sessionKeyPool;
    private final Balance balance;
    //存放可用的主机列表 将会在notify方法进行更新维护操作
    private final ConcurrentHashMap<String, List<URL>> availableHostCache = new ConcurrentHashMap<>();


    public ClusterInvoker(Registry registry, BaseKeyedPooledObjectFactory keyedPooledObjectFactory, Balance balance) {
        this.registry = registry;
        this.sessionKeyPool = new GenericKeyedObjectPool(keyedPooledObjectFactory);
        sessionKeyPool.setMaxTotal(100);
//        sessionKeyPool.setTestOnReturn(true);
        sessionKeyPool.setTestOnBorrow(true);
        this.balance = balance;

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
        RequestSession session = null;
        try {
            session = this.sessionKeyPool.borrowObject(url);
            return session.invoker(request);
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e);
        } finally {
            this.sessionKeyPool.returnObject(url, session);
        }
    }

    @Override
    public void shutdown() {

    }

    //TODO
    @Override
    public void notify(List<URL> urls, CallbackType type, Exception e) {

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
        if (!availableHostCache.contains(key)) {
            Protocol protocol = Protocol
                    .builder()
                    .application(application)
                    .version(version)
                    .group(group)
                    .service(service).build();
            urls = registry.lookup(protocol);
            availableHostCache.put(key, urls);
        }
        return urls;
    }

    private String getServiceKey(String service) {
        return application + "#" + version + "#" + group + "#" + service;
    }



}
