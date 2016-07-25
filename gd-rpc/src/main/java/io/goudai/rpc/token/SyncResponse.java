package io.goudai.rpc.token;

import io.goudai.rpc.exception.RequestTimeoutException;
import io.goudai.rpc.model.Request;
import io.goudai.rpc.model.Response;
import lombok.Getter;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by freeman on 2016/1/17.
 * 包装请求和响应 异步转同步
 */
@Getter
public class SyncResponse {
    private String id;
    private Request request;
    private Response response;
    private CountDownLatch latch = new CountDownLatch(1);


    public SyncResponse(Request request) {
        this.id = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        request.setId(this.id);
        this.request = request;
    }

    public void notifyResponse(Response response) {
        this.response = response;
        this.latch.countDown();
    }

    public Response awaitResponse() {
        try {
            if (!this.latch.await(request.getTimeout(), TimeUnit.MILLISECONDS)) {
                SyncResponseManager.removeSyncResponse(this.id);
                throw new RequestTimeoutException("timeout=[" + this.request + "]");
            }
        } catch (InterruptedException e) {
            //ig
        } catch (RequestTimeoutException e) {
            throw e;
        }
        //未超时的情况下 response会唤醒此方法并给response赋值
        return this.response;
    }
}
