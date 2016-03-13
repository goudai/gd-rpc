package io.goudai.rpc.invoker;

import io.goudai.net.Connector;
import io.goudai.net.session.AbstractSession;
import io.goudai.net.session.factory.SessionFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.net.InetSocketAddress;

/**
 * Created by freeman on 2016/1/31.
 */
@RequiredArgsConstructor
@Getter
public class RequestSessionFactory extends BasePooledObjectFactory<RequestSession> {
    private final String serverIp;
    private final int serverPort;
    private final Connector connector;
    private final SessionFactory sessionFactory;



    @Override
    public RequestSession create() throws Exception {
        return new RequestSession(connector, new InetSocketAddress(serverIp, serverPort), sessionFactory);
    }


    @Override
    public PooledObject<RequestSession> wrap(RequestSession obj) {
        return new DefaultPooledObject<>(obj);
    }

    @Override
    public boolean validateObject(PooledObject<RequestSession> p) {
        return p.getObject().getSession().getStatus() == AbstractSession.Status.OPEN;
    }

    @Override
    public void destroyObject(PooledObject<RequestSession> p) throws Exception {
        p.getObject().getSession().close();
    }
}
