package io.goudai.rpc.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by freeman on 2016/1/17.
 */
@Builder
@Data
public class Request implements Serializable {

    /**
     * id requset的唯一表示
     */
    private String id;

    /**
     * 需要进行调用的服务接口全名
     */
    private String service;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 参数
     */
    private Object[] params;

    /**
     * 参数类型
     */
    private Class<?>[] patamType;


}
