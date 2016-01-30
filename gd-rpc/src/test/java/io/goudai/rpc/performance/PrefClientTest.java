package io.goudai.rpc.performance;

import io.goudai.commons.factory.NamedThreadFactory;
import io.goudai.net.context.Context;
import io.goudai.net.handler.codec.DefaultDecoder;
import io.goudai.net.handler.codec.DefaultEncoder;
import io.goudai.net.handler.serializer.JavaSerializer;
import io.goudai.net.handler.serializer.Serializer;
import io.goudai.rpc.User;
import io.goudai.rpc.UserService;
import io.goudai.rpc.bootstarp.Bootstrap;
import io.goudai.rpc.handler.ResponseHandler;
import io.goudai.rpc.model.Request;
import io.goudai.rpc.model.Response;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Administrator on 2016/1/30.
 */
public class PrefClientTest {
    static {
        Serializer serializer = new JavaSerializer();
        Context.<Request, Response>builder()
                .decoder(new DefaultDecoder<>(serializer))
                .encoder(new DefaultEncoder<>(serializer))
                .serializer(serializer)
                .channelHandler(new ResponseHandler())
                .executorService(Executors.newFixedThreadPool(20, new NamedThreadFactory()))
                .build()
                .init();
    }
    public static void main(String[] args) throws Exception {

        final long N = 1000000;
        final int threadCount = 100;
        final AtomicLong counter = new AtomicLong(0);
        Bootstrap[] bootstraps = new Bootstrap[threadCount];
        Bootstrap bootstrap =  new Bootstrap("localhost", 9999,1);
        bootstrap.startup();

        System.out.println("init success");
        final long startTime = System.currentTimeMillis();
        Task[] tasks = new Task[threadCount];
        for (int i = 0; i < threadCount; i++) {
            tasks[i] = new Task(bootstrap.getService(UserService.class), counter, startTime, N);
        }
        for (Task task : tasks) {
            task.start();

        }

        for (Task task : tasks) {
            task.join();
        }

    }
}
class Task extends Thread {
    private final UserService userService;
    private final AtomicLong counter;
    private final long startTime;
    private final long N;

    public Task( UserService userService, AtomicLong counter, long startTime, long N) {
        this.userService = userService;
        this.counter = counter;
        this.startTime = startTime;
        this.N = N;
    }

    @Override
    public void run() {
        for (int i = 0; i < N; i++) {
            try {
                if(userService == null) System.out.println(userService == null);
               userService.add(new User());
                counter.incrementAndGet();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (counter.get() % 5000 == 0) {
                double qps = counter.get() * 1000.0 / (System.currentTimeMillis() - startTime);
                System.out.format("QPS: %.2f\n", qps);
            }
        }
    }
}