package io.goudai.cluster.bootstarp;

import com.alibaba.fastjson.JSON;
import io.goudai.net.handler.serializer.SerializeException;
import io.goudai.net.handler.serializer.Serializer;
import io.goudai.rpc.model.Request;

/**
 * Created by freeman on 16/9/18.
 */
public class JsonSerizable implements Serializer {

	@Override
	public byte[] serialize(Object obj) throws SerializeException {
		return JSON.toJSONBytes(obj);
	}

	@Override
	public Object deserialize(byte[] bytes) throws SerializeException {
		return JSON.parseObject(bytes, Request.class);
	}
}
