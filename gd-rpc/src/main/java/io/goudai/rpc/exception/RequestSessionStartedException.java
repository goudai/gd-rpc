package io.goudai.rpc.exception;

/**
 * Created by freeman on 2016/1/30.
 */
public class RequestSessionStartedException extends RuntimeException {

    public RequestSessionStartedException() {
    }

    public RequestSessionStartedException(String message) {
        super(message);
    }

    public RequestSessionStartedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestSessionStartedException(Throwable cause) {
        super(cause);
    }

    public RequestSessionStartedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
