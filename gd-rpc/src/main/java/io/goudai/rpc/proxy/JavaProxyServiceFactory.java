package io.goudai.rpc.proxy;

import io.goudai.rpc.exception.RpcException;
import io.goudai.rpc.invoker.Invoker;
import io.goudai.rpc.model.Request;
import io.goudai.rpc.model.Response;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * Created by freeman on 2016/1/17.
 */
@Builder
@RequiredArgsConstructor
public class JavaProxyServiceFactory implements ProxyServiceFactory {
    /*将实际的调用委托到invoker.invoke上进行处理 的*/
    private final Invoker invoker;

    public <T> T createServiceProxy(Class<T> interfaceClass) throws RpcException {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, (proxy, method, args) -> {
            Request request = makeRequest(interfaceClass, method, args);
            Response response = invoker.invoke(request);
            if (response.getException() != null) throw response.getException();
            return response.getResult();
        });
    }

    public <T> Request makeRequest(Class<T> klass, Method method, Object[] args) {
        return Request.builder()
                .id(UUID.randomUUID().toString())
                .service(klass.getName())
                .params(args)
                .patamType(method.getParameterTypes())
                .methodName(method.getName())
                .build();
    }
}
