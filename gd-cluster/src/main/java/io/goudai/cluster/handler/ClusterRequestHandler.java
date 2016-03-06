package io.goudai.cluster.handler;

import io.goudai.cluster.util.MethodUtil;
import io.goudai.registry.Registry;
import io.goudai.registry.protocol.Protocol;
import io.goudai.rpc.handler.RequestHandler;
import lombok.extern.slf4j.Slf4j;

import static io.goudai.cluster.config.ClusterConfig.*;
/**
 * Created by freeman on 2016/3/6.
 */
@Slf4j
public class ClusterRequestHandler extends RequestHandler {

    private Registry registry;

    public ClusterRequestHandler(Registry registry) {
        this.registry = registry;
    }

    @Override
    public void registry(Class<?> interClass, Object service) {
        //注册服务到注册中心
        Protocol protocol = Protocol
                .builder()
                .application(application)
                .version(version)
                .group(group)
                .host(host)
                .port(String.valueOf(port))
                .service(interClass.getName())
                .timeout(timeout)
                .type(PROVIDER)
                .methods(MethodUtil.getMethods(interClass))
                .build();
        registry.register(protocol);
        log.info("register service to registry --> {}",protocol);
        super.registry(interClass, service);
    }


}
