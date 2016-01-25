package io.goudai.net.handler;

import io.goudai.net.session.AbstractSession;

/**
 * Created by freeman on 2016/1/12.
 */
public interface ChannelHandler {

    /**
     * 在进行了编解码之后的消息将发送到此处
     * @param session
     * @param object
     */
    void received(AbstractSession session,Object object);

}
