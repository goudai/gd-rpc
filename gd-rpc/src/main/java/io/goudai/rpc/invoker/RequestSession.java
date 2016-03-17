package io.goudai.rpc.invoker;

import io.goudai.net.Connector;
import io.goudai.net.session.Session;
import io.goudai.net.session.factory.SessionFactory;
import io.goudai.rpc.exception.ChannelClosedException;
import io.goudai.rpc.exception.RequestSessionStartedException;
import io.goudai.rpc.model.Request;
import io.goudai.rpc.model.Response;
import io.goudai.rpc.token.SyncResponse;
import io.goudai.rpc.token.SyncResponseManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * Created by freeman on 2016/1/17.
 */
//@RequiredArgsConstructor
@Getter
@Slf4j
public class RequestSession {

    private final Connector connector;
    private final InetSocketAddress remoteAddress;
    private final SessionFactory sessionFactory;
    ChannelClosedException channelClosedException = new ChannelClosedException();
    private Session session;
    //TODO 暂时写死 以后移动到配置文件中
    private long timeout = 3000;

    public RequestSession(Connector connector, InetSocketAddress remoteAddress, SessionFactory sessionFactory) {
        this.connector = connector;
        this.remoteAddress = remoteAddress;
        this.sessionFactory = sessionFactory;
        try {
            session = connector.connect(this.remoteAddress, timeout, sessionFactory);
        } catch (Exception e) {
            throw new RequestSessionStartedException(e);
        }
    }

    public Response invoker(Request request) {
        SyncResponse syncResponse = SyncResponseManager.createSyncResponse(request);
        boolean write = this.session.write(request);
        if (!write) {
            SyncResponseManager.removeSyncResponse(syncResponse.getId()).getResponse();
            throw channelClosedException;
        }
        return syncResponse.awaitResponse();


    }


}
