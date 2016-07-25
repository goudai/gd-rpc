package io.goudai.rpc.model;


import lombok.Builder;

/**
 * Created by freeman on 2016/1/17.
 */
@lombok.Getter
@lombok.Setter
@Builder
public class Response implements java.io.Serializable {
    private String id;
    private Object result;
    private Throwable exception;

}
