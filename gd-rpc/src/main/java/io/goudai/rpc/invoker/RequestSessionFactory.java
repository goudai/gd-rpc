package io.goudai.rpc.invoker;

import io.goudai.commons.pool.factory.ObjectFactory;
import io.goudai.net.Connector;
import io.goudai.net.session.factory.SessionFactory;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;

/**
 * Created by freeman on 2016/1/28.
 */
@RequiredArgsConstructor
public class RequestSessionFactory implements ObjectFactory<RequestSession> {
    private final String serverIp;
    private final int serverPort;
    private final Connector connector;
    private final SessionFactory sessionFactory;

    @Override
    public RequestSession create() {
        try {
            return new RequestSession(new InetSocketAddress(serverIp, serverPort), connector, sessionFactory);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
