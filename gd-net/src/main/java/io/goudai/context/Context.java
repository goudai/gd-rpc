package io.goudai.context;

import io.goudai.handler.codec.factory.ByteToObjectDecoderFactory;
import io.goudai.handler.in.factory.ChannelHandlerFactory;
import io.goudai.handler.serializer.factory.SerializerFactory;

/**
 * Created by freeman on 2016/1/12.
 */

public class Context {

    //TODO 返回具体T的 ByteToPacketDecoderFactory 实现类
    public static <T> ByteToObjectDecoderFactory<T> getByteToObjectDecoderFactory(){
        return null;
    }

    public static <T> ChannelHandlerFactory<T> getChannelHandlerFactory(){
        return null;
    }

    public static SerializerFactory getSerializerFactory(){
        return null;
    }
}
