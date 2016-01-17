package io.goudai.rpc.model;

import java.io.Serializable;

/**
 * Created by freeman on 2016/1/17.
 */
public class Response implements Serializable {

    private String id;
    private Object result;
    private Throwable exception;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        return "Response{" +
                "id='" + id + '\'' +
                ", result=" + result +
                ", exception=" + exception +
                '}';
    }
}
