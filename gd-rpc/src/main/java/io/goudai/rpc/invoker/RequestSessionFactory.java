package io.goudai.rpc.invoker;

import io.goudai.commons.pool.factory.ObjectFactory;
import io.goudai.net.Connector;
import io.goudai.net.session.factory.SessionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * Created by freeman on 2016/1/28.
 */
@RequiredArgsConstructor
@Slf4j
public class RequestSessionFactory implements ObjectFactory<RequestSession> {
    private final String serverIp;
    private final int serverPort;
    private final Connector connector;
    private final SessionFactory sessionFactory;

    @Override
    public RequestSession create() {
        try {
            RequestSession requestSession = new RequestSession(connector, new InetSocketAddress(serverIp, serverPort), sessionFactory);
            log.info("connected success [{}]", requestSession.getSession());
            return requestSession;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void destroy(RequestSession object) {
        try {
            object.getSession().close();
        } catch (Exception e) {

        }
    }
}
