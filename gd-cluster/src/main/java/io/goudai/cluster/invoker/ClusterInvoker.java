package io.goudai.cluster.invoker;

import io.goudai.cluster.balance.Balance;
import io.goudai.registry.Registry;
import io.goudai.registry.protocol.Protocol;
import io.goudai.registry.protocol.URL;
import io.goudai.registry.zookeeper.Callback;
import io.goudai.rpc.exception.RpcException;
import io.goudai.rpc.invoker.Invoker;
import io.goudai.rpc.invoker.RequestSession;
import io.goudai.rpc.model.Request;
import io.goudai.rpc.model.Response;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
    /**
     * 存储服务及其对应的主机列表
     * service.UserService#test#1.1.1#2 -> List[provider://host:port,provider://host:port]
     */
    private final ConcurrentHashMap<String, List<URL>> concurrentHashMap = new ConcurrentHashMap<>();


    public ClusterInvoker(Registry registry, GenericKeyedObjectPool<URL, RequestSession> sessionKeyPool, BaseKeyedPooledObjectFactory keyedPooledObjectFactory, Balance balance) {
        this.registry = registry;
        this.sessionKeyPool = sessionKeyPool;
        this.balance = balance;

    }

    @Override
    public Response invoke(Request request) throws RpcException {
        String key = getServiceKey(request);
        final URL url = this.balance.select(concurrentHashMap.get(key));
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

    private String getServiceKey(Request request) {
        return request.getService() + "#";
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void notify(List<Protocol> protocols, Exception e) {

    }


}
