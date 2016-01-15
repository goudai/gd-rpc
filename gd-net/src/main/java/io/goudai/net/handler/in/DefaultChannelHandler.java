package io.goudai.net.handler.in;

import io.goudai.net.session.AbstractSession;

/**
 * Created by Administrator on 2016/1/12.
 */
public class DefaultChannelHandler<T> implements ChannelInHandler<T> {

    // TODO 思考是否将线程池交给用户控制 如果不交个用户控制 此处讲传入一个request

    @Override
    public void received(AbstractSession session, T request) {
        System.out.println(request);
        System.out.println("我是服务器的handler");
        session.write("你好");
    }
}
