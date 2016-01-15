package io.goudai.commons.factory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by freeman on 2016/1/15.
 */
public class NamedThreadFactory implements ThreadFactory {

    private static final AtomicInteger POOL_SEQ = new AtomicInteger(1);
    private final AtomicInteger mThreadNum;
    private final String mPrefix;
    private final boolean mDaemo;
    private final ThreadGroup mGroup;

    public NamedThreadFactory() {
        this("pool-" + POOL_SEQ.getAndIncrement(), false);
    }

    public NamedThreadFactory(String prefix) {
        this(prefix, false);
    }

    public NamedThreadFactory(String prefix, boolean daemo) {
        this.mThreadNum = new AtomicInteger(1);
        this.mPrefix = prefix + "-thread-";
        this.mDaemo = daemo;
        SecurityManager s = System.getSecurityManager();
        this.mGroup = s == null?Thread.currentThread().getThreadGroup():s.getThreadGroup();
    }

    public Thread newThread(Runnable runnable) {
        String name = this.mPrefix + this.mThreadNum.getAndIncrement();
        Thread ret = new Thread(this.mGroup, runnable, name, 0L);
        ret.setDaemon(this.mDaemo);
        return ret;
    }

    public ThreadGroup getThreadGroup() {
        return this.mGroup;
    }
}
