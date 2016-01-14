package io.goudai.handler.codec;

import io.goudai.handler.serializer.JavaSerializer;
import io.goudai.handler.serializer.Serializer;

import java.nio.ByteBuffer;

public class DefaultEncoder<T> implements Encoder<T> {

	@Override
	public ByteBuffer encode(T response) {
		Serializer serializer = new JavaSerializer(); // TODO 动态
		byte[] bytes = serializer.serialize(response);
		int length = bytes.length;
		ByteBuffer byteBuffer = ByteBuffer.allocate(length + 4);
		byteBuffer.putInt(length);
		byteBuffer.put(bytes);
		return byteBuffer;
	}

    
}
