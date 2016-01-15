package io.goudai.net.handler.in;

import io.goudai.net.session.AbstractSession;

/**
 * Created by Administrator on 2016/1/12.
 */
public interface ChannelInHandler<T> {

    /**
     * 在进行了编解码之后的消息将发送到此处
     * @param session
     * @param request
     */
    void received(AbstractSession session,T request);

}
