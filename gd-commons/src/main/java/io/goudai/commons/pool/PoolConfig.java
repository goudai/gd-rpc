package io.goudai.commons.pool;

import lombok.Builder;
import lombok.Data;

/**
 * Created by freeman on 2016/1/17.
 */
@Builder
@Data
public class PoolConfig {
    /**
     * 初始化池大小
     */
    private int init = 64;
    /**
     * 池中最大数量
     */
    private int max = 64;

}
