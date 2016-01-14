package io.goudai.handler.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by freeman on 2016/1/12.
 */
public class JavaSerializer implements Serializer {

	@Override
	public byte[] serialize(Object obj) throws SerializeException{
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(baos)) {
			oos.writeObject(obj);
			oos.flush();
			return baos.toByteArray();
		} catch (Exception e) {
			throw new SerializeException("java serialize error", e);
		}
	}

	@Override
	public Object deserialize(byte[] bytes) throws SerializeException{
		try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes); ObjectInputStream ois = new ObjectInputStream(bais)) {
			return ois.readObject();
		} catch (Exception e) {
			throw new SerializeException("java deserialize error", e);
		}
	}
}
