package io.goudai.rpc.invoker;

import io.goudai.commons.pool.Pool;
import io.goudai.commons.pool.impl.Commons2Pool;
import io.goudai.rpc.exception.RpcException;
import io.goudai.rpc.model.Request;
import io.goudai.rpc.model.Response;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Created by freeman on 2016/1/17.
 */
public class SingleInvoker implements Invoker {

    private Pool<RequestSession> requestSessionPool;


    public SingleInvoker(PooledObjectFactory<RequestSession> objectFactory) {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxIdle(100);
        config.setMaxTotal(100);
//        config.setTestOnReturn(true);
        this.requestSessionPool = new Commons2Pool<>(config, objectFactory);
    }

    public SingleInvoker(PooledObjectFactory<RequestSession> objectFactory, GenericObjectPoolConfig config) {
        config.setTestOnReturn(true);
        this.requestSessionPool = new Commons2Pool<>(config, objectFactory);
    }
    

    @Override
    public String name() {
        return SingleInvoker.class.getSimpleName();
    }

    @Override
    public Response invoke(Request request) throws RpcException {
        Response response = Response.builder().id(request.getId()).build();
        RequestSession requestSession = null;
        try {
            requestSession = this.requestSessionPool.borrowObject();
            response = requestSession.invoker(request);
        } catch (Exception e) {
            response.setException(e);
        } finally {
            this.requestSessionPool.returnObject(requestSession);
        }
        return response;
    }

    @Override
    public void shutdown() {
        this.requestSessionPool.destroy();
    }

}
