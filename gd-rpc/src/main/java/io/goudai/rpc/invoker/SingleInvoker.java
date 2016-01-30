package io.goudai.rpc.invoker;

import io.goudai.commons.pool.Pool;
import io.goudai.commons.pool.PoolConfig;
import io.goudai.commons.pool.factory.ObjectFactory;
import io.goudai.commons.pool.impl.Commons2Pool;
import io.goudai.rpc.exception.RpcException;
import io.goudai.rpc.model.Request;
import io.goudai.rpc.model.Response;

/**
 * Created by freeman on 2016/1/17.
 */
public class SingleInvoker implements Invoker {

    private Pool<RequestSession> requestSessionPool;

    public SingleInvoker(ObjectFactory<RequestSession> requestSessionObjectFactory, PoolConfig poolConfig) {
        this.requestSessionPool = new Commons2Pool<>(requestSessionObjectFactory);
    }


    @Override
    public String name() {
        return "SingleInvoker";
    }

    @Override
    public Response invoke(Request request) throws RpcException {
        Response response = Response.builder().build();
        RequestSession requestSession = null;
        try {
            requestSession = this.requestSessionPool.borrowObject();
            if (requestSession == null) System.out.println("request is null ");
            response = requestSession.invoker(request);
            if (response == null) System.out.println("response is null !");
        }  catch (Exception e) {
            response.setId(request.getId());
            response.setException(e);
        } finally {
            this.requestSessionPool.returnObject(requestSession);
        }
        return response;
    }


}
