package io.goudai.cluster.balance;

import io.goudai.registry.protocol.URL;
import io.goudai.rpc.exception.RpcException;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by freeman on 2016/3/4.
 */
public interface Balance {

    AtomicInteger atomicInteger = new AtomicInteger();

    default URL select(List<URL> urls) throws RpcException{
        int i = this.atomicInteger.getAndIncrement() % urls.size();
        if (i < 0) {
            this.atomicInteger.set(0);
            i = 0;
        }
        return urls.get(i);
    }
}
