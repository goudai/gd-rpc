package io.goudai.rpc.invoker;


import io.goudai.rpc.exception.RpcException;
import io.goudai.rpc.model.Request;
import io.goudai.rpc.model.Response;

/**
 * Created by freeman on 2016/1/17.
 */
public interface Invoker {


    default String name() {
        return this.getClass().getSimpleName();
    }

    Response invoke(Request request) throws RpcException;

    void shutdown();

}
