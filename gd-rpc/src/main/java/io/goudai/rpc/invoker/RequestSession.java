package io.goudai.rpc.invoker;

import io.goudai.net.Connector;
import io.goudai.net.session.Session;
import io.goudai.net.session.factory.SessionFactory;
import io.goudai.rpc.exception.RequestSessionStartedException;
import io.goudai.rpc.exception.RequestTimeoutException;
import io.goudai.rpc.model.Request;
import io.goudai.rpc.model.Response;
import io.goudai.rpc.token.Token;
import io.goudai.rpc.token.TokenManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

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

    public Response invoker(Request request) throws RequestTimeoutException, InterruptedException {
        Token token = TokenManager.createTicket(request, timeout);
        this.session.write(request);
        if (!token.await(timeout, TimeUnit.MILLISECONDS)) {
            throw new RequestTimeoutException("timeout=[" + this.timeout + "] requestId= [" + request.getId() + "]!");
        }
        return token.getResponse();


    }


}
