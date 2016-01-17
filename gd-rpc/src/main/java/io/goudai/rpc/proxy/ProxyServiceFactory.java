package io.goudai.rpc.proxy;

import io.goudai.rpc.exception.RpcException;

/**
 * Created by Administrator on 2016/1/17.
 */
public interface ProxyServiceFactory {

    /*返回service代理实例*/
     <T> T createServiceProxy(Class<T> interfaceClass) throws RpcException;
}
