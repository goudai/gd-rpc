package io.goudai.rpc.proxy;

import io.goudai.rpc.exception.RpcException;
import io.goudai.rpc.invoker.Invoker;
import io.goudai.rpc.model.Request;
import io.goudai.rpc.model.Response;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * Created by freeman on 2016/1/17.
 */
public class JavaProxyServiceFactory implements ProxyServiceFactory {
    /*将实际的调用委托到invoker.invoke上进行处理 的*/
    private final Invoker invoker;

    public JavaProxyServiceFactory(Invoker invoker) {
        this.invoker = invoker;
    }


    public <T> T createServiceProxy(Class<T> interfaceClass) throws RpcException {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, (proxy, method, args) -> {
            Request request = makeRequest(interfaceClass, method, args);
            Response response = invoker.invoke(request);
            if (response.getException() != null) throw response.getException();
            return response.getResult();
        });
    }


    public  <T> Request makeRequest(Class<T> klass, Method method, Object[] args) {
        Request request = new Request();
        request.setId(UUID.randomUUID().toString());
        request.setService(klass.getName());
        request.setParams(args);
        Class<?>[] parameterTypes = method.getParameterTypes();
        request.setPatamType(parameterTypes);
        request.setMethodName(method.getName());
        return request;
    }
}
