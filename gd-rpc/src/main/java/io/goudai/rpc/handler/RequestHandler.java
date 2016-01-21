package io.goudai.rpc.handler;

import io.goudai.net.handler.ChannelHandler;
import io.goudai.net.session.AbstractSession;
import io.goudai.rpc.model.Request;
import io.goudai.rpc.model.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by freeman on 2016/1/17.
 * 处理客户端发送的请求
 */
public class RequestHandler implements ChannelHandler {
    private Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    /*换成接口*/
    public ConcurrentHashMap<String, Object> services = new ConcurrentHashMap<>();
    /*缓存方法*/
    public ConcurrentHashMap<String, Method> methodConcurrentHashMap = new ConcurrentHashMap<>();

    @Override
    public void received(AbstractSession session, Object obj) {

        if(obj instanceof Request){
            Request request = (Request) obj;
            Response response = new Response();
            response.setId(request.getId());
            try {
                response.setResult(findMethod(request).invoke(services.get(request.getService()), request.getParams()));
            }catch (NullPointerException e){
                logger.error(e.getMessage(), e);
                response.setException(e);
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
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
