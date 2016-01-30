package io.goudai.rpc.invoker;

import io.goudai.net.Connector;
import io.goudai.net.session.Session;
import io.goudai.net.session.factory.SessionFactory;
import io.goudai.rpc.exception.RpcException;
import io.goudai.rpc.model.Request;
import io.goudai.rpc.model.Response;
import io.goudai.rpc.token.Token;
import io.goudai.rpc.token.TokenManager;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Created by freeman on 2016/1/17.
 */
public class RequestSession {

    private Session session;
    private long timeout = 3000;
   private Connector connector;
    private InetSocketAddress remoteAddress;

    public RequestSession(InetSocketAddress remoteAddress, Connector connector, SessionFactory sessionFactory) throws Exception {
        this.remoteAddress = remoteAddress;
        this.connector = connector;
        session = connector.connect(this.remoteAddress,timeout,sessionFactory);
    }


    public Response invoker(Request request) throws RpcException {
        Token token = TokenManager.createTicket(request, timeout);
        try {
            this.session.write(request);
            if (!token.await(timeout, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("request time out !!");
            }
            return token.getResponse();
        } catch (Exception e) {
            throw new RpcException(e.getMessage(),e);
        }

    }

    @Override
    public String toString() {
        return "Client{" +
                ", session=" + session +
                ", timeout=" + timeout +
                '}';
    }

}
