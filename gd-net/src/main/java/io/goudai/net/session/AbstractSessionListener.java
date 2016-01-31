package io.goudai.net.session;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by freeman on 2016/1/30.
 */
@Slf4j
public class AbstractSessionListener implements SessionListener {

    @Override
    public void onCreated(AbstractSession session) {
        log.info("connected success [{}]",session);
        session.setStatus(AbstractSession.Status.NEW);
    }

    @Override
    public void onMessage(AbstractSession session, Object obj) {

    }

    @Override
    public void onDestory(AbstractSession session) {
        session.setStatus(AbstractSession.Status.CLOSED);
    }

    @Override
    public void onException(AbstractSession session, Exception e) {
        try {
            session.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        if(e.getMessage().contains("远程主机强迫关闭了一个现有的连接")){
            log.warn("disconnect session [{}]",session);
            return;
        }
        log.warn(session.toString(), e);

    }
}
