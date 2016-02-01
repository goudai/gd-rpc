package io.goudai.rpc.handler;

import io.goudai.net.handler.ChannelHandler;
import io.goudai.net.session.AbstractSession;
import io.goudai.rpc.exception.ServiceNotRegistryException;
import io.goudai.rpc.model.Heartbeat;
import io.goudai.rpc.model.Request;
import io.goudai.rpc.model.Response;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by freeman on 2016/1/17.
 * 处理客户端发送的请求
 */
@Slf4j
public class RequestHandler implements ChannelHandler, ServiceRegistryHandler {
    /*换成接口*/
    public final ConcurrentHashMap<String, Object> services = new ConcurrentHashMap<>();
    /*缓存方法*/
    public final ConcurrentHashMap<String, Method> methodConcurrentHashMap = new ConcurrentHashMap<>();

    @Override
    public void received(AbstractSession session, Object obj) {

        if (obj instanceof Request) {
            Request request = (Request) obj;
            Response response = Response.builder().id(request.getId()).build();
            try {
                response.setResult(findMethod(request).invoke(services.get(request.getService()), request.getParams()));
            } catch (Throwable e) {
                response.setException(e);
            } finally {
                //timeout,not send response
                long timeout = System.currentTimeMillis() - request.getCreateTime();
                if (timeout > request.getTimeout()) {
                    if (log.isWarnEnabled())
                        log.debug("request timeout,not send response!  invoker time =[{}]. request set timeout = [{}].retquest [{}]", timeout, request.getTimeout(), request);
                } else {
                    session.write(response);
                }
            }
        } else if (obj instanceof Heartbeat) {
            //处理心跳信息
            if(log.isInfoEnabled())
                log.info("heartbeat msg , session = [{}]",session);
        }



    }

    private Method findMethod(Request request) throws NoSuchMethodException {
        String key = request.getService() + "#" + request.getMethodName();
        if (!this.methodConcurrentHashMap.contains(key)) {
            String service = request.getService();
            String methodName = request.getMethodName();
            Class<?>[] patamType = request.getPatamType();
            Object o = services.get(service);
            if (o == null) throw new ServiceNotRegistryException("No services [ " + request.getService() + " ]");
            Method method = o.getClass().getMethod(methodName, patamType);
            this.methodConcurrentHashMap.put(key, method);
        }
        return this.methodConcurrentHashMap.get(key);
    }


    @Override
    public void registry(Class<?> interClass, Object service) {
        this.services.putIfAbsent(interClass.getName(), service);
    }
}
