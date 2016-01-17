package io.goudai.rpc.handler;

import io.goudai.net.handler.in.ChannelInHandler;
import io.goudai.net.session.AbstractSession;
import io.goudai.rpc.model.Response;
import io.goudai.rpc.token.Token;
import io.goudai.rpc.token.TokenManager;

/**
 * Created by freeman on 2016/1/17.
 * 处理服务端返回的响应
 */
public class ResponeHandler implements ChannelInHandler<Response> {
    @Override
    public void received(AbstractSession session, Response response) {
        Token ticket = TokenManager.removeTicket(response.getId());
        if (ticket != null) {
            ticket.notifyResponse(response);
        }
    }
}
