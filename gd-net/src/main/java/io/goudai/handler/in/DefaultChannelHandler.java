package io.goudai.handler.in;

import io.goudai.session.AbstractSession;

import java.util.List;

/**
 * Created by Administrator on 2016/1/12.
 */
public class DefaultChannelHandler<T> implements ChannelInHandler<T> {

    // TODO 思考是否将线程池交给用户控制 如果不交个用户控制 此处讲传入一个request

    @Override
    public void received(AbstractSession session, List<T> request) {
        //
    }
}
