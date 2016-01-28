package io.goudai.rpc.invoker;

import io.goudai.commons.pool.Pool;
import io.goudai.commons.pool.PoolConfig;
import io.goudai.commons.pool.factory.ObjectFactory;
import io.goudai.commons.pool.impl.JavaPool;
import io.goudai.rpc.exception.RpcException;
import io.goudai.rpc.model.Request;
import io.goudai.rpc.model.Response;

/**
 * Created by freeman on 2016/1/17.
 */
public class SingleInvoker implements Invoker {

    private Pool<RequestSession> requestSessionPool;

    public SingleInvoker(ObjectFactory<RequestSession> requestSessionObjectFactory, PoolConfig poolConfig) {
        this.requestSessionPool = new JavaPool<>(requestSessionObjectFactory, poolConfig);
    }



    @Override
    public String name() {
        return "SingleInvoker";
    }

    @Override
    public Response invoke(Request request) throws RpcException {
        Response response = null;
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


}
