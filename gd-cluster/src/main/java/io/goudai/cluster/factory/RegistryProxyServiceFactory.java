package io.goudai.cluster.factory;

import io.goudai.registry.Registry;
import io.goudai.rpc.exception.RpcException;
import io.goudai.rpc.invoker.Invoker;
import io.goudai.rpc.proxy.JavaProxyServiceFactory;

/**
 * Created by freeman on 2016/3/6.
 */
public class RegistryProxyServiceFactory extends JavaProxyServiceFactory {

    private final Registry registry;

    public RegistryProxyServiceFactory(Invoker invoker, Registry registry) {
        super(invoker);
        this.registry = registry;
    }


    @Override
    public <T> T createServiceProxy(Class<T> interfaceClass) throws RpcException {

        T serviceProxy = super.createServiceProxy(interfaceClass);
        return serviceProxy;
    }


}
