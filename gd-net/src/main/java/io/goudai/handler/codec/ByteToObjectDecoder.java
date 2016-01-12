package io.goudai.handler.codec;

import io.goudai.buffer.IoBuffer;

import java.util.List;

/**
 * Created by freeman on 2016/1/12.
 * 抽象 网络数据包解析到对象
 */
public interface ByteToObjectDecoder<T> {

    IoBuffer decode(IoBuffer in,List<T> requests);

}
