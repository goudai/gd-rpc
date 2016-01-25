package io.goudai.rpc.handler;

import io.goudai.net.handler.ChannelHandler;
import io.goudai.net.session.AbstractSession;
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
public class RequestHandler implements ChannelHandler {
    /*换成接口*/
    public final ConcurrentHashMap<String, Object> services = new ConcurrentHashMap<>();
    /*缓存方法*/
    public final ConcurrentHashMap<String, Method> methodConcurrentHashMap = new ConcurrentHashMap<>();

    @Override
    public void received(AbstractSession session, Object obj) {

        if(obj instanceof Request){
            Request request = (Request) obj;
            Response response = Response.builder().id(request.getId()).build();
            try {
                response.setResult(findMethod(request).invoke(services.get(request.getService()), request.getParams()));
            }catch (NullPointerException e){
                log.error(e.getMessage(), e);
                response.setException(e);
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                response.setException(e);
            }finally {
                session.write(response);
            }
        }


    }

    private Method findMethod(Request request) throws NoSuchMethodException {
        String key = request.getService() + request.getMethodName();
        if (!this.methodConcurrentHashMap.contains(key))
            this.methodConcurrentHashMap.put(key, services.get(request.getService()).getClass().getMethod(request.getMethodName(), request.getPatamType()));
        return this.methodConcurrentHashMap.get(key);
    }

    public void service(Class<?> interfaceClass, Object service) {
        this.services.putIfAbsent(interfaceClass.getName(), service);
    }
}
