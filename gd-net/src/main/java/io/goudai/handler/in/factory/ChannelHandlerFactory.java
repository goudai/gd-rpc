package io.goudai.handler.in.factory;

import io.goudai.handler.in.ChannelInHandler;

/**
 * Created by Administrator on 2016/1/12.
 */
public interface ChannelHandlerFactory<T> {
    /**
     * 返回具体的handler处理器
     * @return
     */
    ChannelInHandler<T> make();
}
