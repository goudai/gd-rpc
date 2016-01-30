package io.goudai.rpc.exception;

/**
 * Created by Administrator on 2016/1/30.
 */
public class RequestTimeoutException extends RuntimeException {
    public RequestTimeoutException() {
    }

    public RequestTimeoutException(String message) {
        super(message);
    }

    public RequestTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestTimeoutException(Throwable cause) {
        super(cause);
    }

    public RequestTimeoutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
