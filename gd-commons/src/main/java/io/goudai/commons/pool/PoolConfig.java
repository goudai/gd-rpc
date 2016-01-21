package io.goudai.commons.pool;

/**
 * Created by freeman on 2016/1/17.
 */
public class PoolConfig {
    /**
     * 初始化池大小
     */
    private int init = 64;
    /**
     * 池中最大数量
     */
    private int max = 64;

    public int getInit() {
        return init;
    }

    public void setInit(int init) {
        this.init = init;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }
}
