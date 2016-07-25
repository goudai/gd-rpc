package io.goudai.net.handler.serializer;


/**
 * Created by freeman on 2016/1/12.
 */
public interface Serializer {

    byte[] serialize(Object obj) throws SerializeException;

    Object deserialize(byte[] bytes) throws SerializeException;
}
