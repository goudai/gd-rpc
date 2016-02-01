package io.goudai.rpc.listener;

import io.goudai.commons.factory.NamedThreadFactory;
import io.goudai.net.session.AbstractSession;
import io.goudai.rpc.model.Heartbeat;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by freeman on 2016/1/30.
 */
@Slf4j
public class SessionManager implements AutoCloseable {
    public final ConcurrentHashMap<Long, AbstractSession> sessionConcurrentHashMap = new ConcurrentHashMap();
    //心跳间隔 默认120s
    private int heartbeatInterval = 120;

    private SessionManager() {
        ScheduledExecutorService heartbeatExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("heartbeat", true));
        //启动心跳
        heartbeatExecutorService.schedule(() -> {
            Iterator<AbstractSession> iterator = sessions().iterator();
            while (iterator.hasNext()){
                AbstractSession session = iterator.next();
                if(session.isClosed()){
                    iterator.remove();
                    continue;
                }
                //得到最后进行通信的时间差
                long update = (System.currentTimeMillis() - session.getUpdateTime()) / 1000L;
                if (update > heartbeatInterval) {
                    session.write(Heartbeat.getInstance());
                    log.info("send heartbeat msg [{}].heartbeat interval [{}]", Heartbeat.getInstance(), this.heartbeatInterval);
                }
            }

        }, heartbeatInterval, TimeUnit.SECONDS);
    }

    public static SessionManager getInstance() {
        return SessionManagerHolder.MANAGER;
    }

    public void add(AbstractSession session) {
        sessionConcurrentHashMap.put(session.getId(), session);
    }

    public Set<Long> getKeys() {
        return this.sessionConcurrentHashMap.keySet();
    }

    public Collection<AbstractSession> sessions() {
        return this.sessionConcurrentHashMap.values();
    }

    @Override
    public void close() throws Exception {
        this.sessions().forEach(s -> {
            try {
                log.info("close session [{}]", s);
                s.close();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    public void remove(AbstractSession session) {
        this.sessionConcurrentHashMap.get(session.getId()).setStatus(AbstractSession.Status.CLOSED);
    }

    private static final class SessionManagerHolder {
        private static final SessionManager MANAGER = new SessionManager();
    }

}
