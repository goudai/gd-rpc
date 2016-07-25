package io.goudai.rpc.exception;

/**
 * Created by vip on 2016/1/28.
 */
public class ServiceNotRegistryException extends RuntimeException {
    public ServiceNotRegistryException() {
    }

    public ServiceNotRegistryException(String message) {
        super(message);
    }

    public ServiceNotRegistryException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceNotRegistryException(Throwable cause) {
        super(cause);
    }

    public ServiceNotRegistryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
