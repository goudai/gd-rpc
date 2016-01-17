package io.goudai.rpc.handler;

import io.goudai.net.handler.in.ChannelInHandler;
import io.goudai.net.session.AbstractSession;
import io.goudai.rpc.model.Request;

/**
 * Created by freeman on 2016/1/17.
 * 处理客户端发送的请求
 */
public class RequestHandler implements ChannelInHandler<Request> {

    //TODO 此处接受到client的请求 进行处理 并使用session写回response
    @Override
    public void received(AbstractSession session, Request request) {

    }
}
