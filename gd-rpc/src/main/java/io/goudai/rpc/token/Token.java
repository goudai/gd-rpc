package io.goudai.rpc.token;

import io.goudai.rpc.model.Request;
import io.goudai.rpc.model.Response;
import lombok.Getter;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by freeman on 2016/1/17.
 */
@Getter
public class Token {
    private String id;
    private Request request;
    private Response response;
    private long timeout;
    private CountDownLatch latch = new CountDownLatch(1);


    public Token(Request request, long timeout) {
        this.id = UUID.randomUUID().toString()+"-"+UUID.randomUUID().toString();
        this.timeout = timeout;
        if (request != null) {
            request.setId(this.id);
        }
        this.request = request;
    }
    public void notifyResponse(Response response) {
        this.response = response;
        this.latch.countDown();
    }

    public boolean await(long timeout, TimeUnit milliseconds) throws InterruptedException {
        return this.latch.await(timeout, milliseconds);
    }
}
