package io.goudai.rpc.invoker;

import io.goudai.commons.pool.Pool;
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

    public SingleInvoker(ObjectFactory<RequestSession> requestSessionObjectFactory) {
        this.requestSessionPool = new Commons2Pool<>(requestSessionObjectFactory);

    }
    @Override
    public String name() {
        return "SingleInvoker";
    }

    @Override
    public Response invoke(Request request) throws RpcException {
        Response response = Response.builder().id(request.getId()).build();
        RequestSession requestSession = null;
        try {
            requestSession = this.requestSessionPool.borrowObject();
            response = requestSession.invoker(request);
        }  catch (Exception e) {
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
