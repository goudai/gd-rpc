package io.goudai.rpc.token;

import io.goudai.rpc.model.Request;
import io.goudai.rpc.model.Response;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by freeman on 2016/1/17.
 */
public class Token {
    private String id;
    private CountDownLatch latch = new CountDownLatch(1);
    private Request request;
    private Response response;


    public Token(Request request, long timeout) {
        this.id = UUID.randomUUID().toString();
        if (request != null) {
            request.setId(this.id);
        }
        this.request = request;
    }

    public String getId() {
        return id;
    }

    public Request request() {
        return this.request;
    }

    public Response response() {
        return this.response;
    }

    public void notifyResponse(Response response) {
        this.response = response;
        this.latch.countDown();
    }

    public boolean await(long timeout, TimeUnit milliseconds) throws InterruptedException {
        return this.latch.await(timeout, milliseconds);
    }
}
