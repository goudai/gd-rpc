package io.goudai.rpc.model;

import java.io.Serializable;

/**
 * Created by freeman on 2016/1/30.
 * 心跳对象
 */

public class Heartbeat implements Serializable {
    private Heartbeat() {
    }

    public static Heartbeat getInstance() {
        return HeartbeatHolder.instance;
    }

    private Object readResolve() {
        return HeartbeatHolder.instance;
    }

    private static class HeartbeatHolder {
        private final static Heartbeat instance = new Heartbeat();
    }
}
