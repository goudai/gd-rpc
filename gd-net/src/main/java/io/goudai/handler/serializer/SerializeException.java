package io.goudai.handler.serializer;

public class SerializeException extends RuntimeException {

	private static final long serialVersionUID = 3101346581215237668L;

	public SerializeException() {
		super();
	}

	public SerializeException(String message) {
		super(message);
	}

	public SerializeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SerializeException(Throwable cause) {
		super(cause);
	}

}
