package io.goudai.rpc.model;

import lombok.*;

import java.io.Serializable;

/**
 * Created by freeman on 2016/1/17.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Request implements Serializable {

    private String id;
    private String service;
    private String methodName;
    private Object[] params;
    private Class<?>[] paramTypes;
    public long createTime;
    public long timeout;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Request{");
        sb.append("id='").append(id).append('\'');
        sb.append(", service='").append(service).append('\'');
        sb.append(", methodName='").append(methodName).append('\'');
        sb.append(", timeout=").append(timeout);
        sb.append('}');
        return sb.toString();
    }
}
