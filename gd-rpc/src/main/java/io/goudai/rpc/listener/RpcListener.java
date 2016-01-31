package io.goudai.rpc.listener;

import io.goudai.net.session.AbstractSession;
import io.goudai.net.session.AbstractSessionListener;

/**
 * Created by freeman on 2016/1/30.
 */
public class RpcListener extends AbstractSessionListener {
    @Override
    public void onCreated(AbstractSession session) {
        super.onCreated(session);
        SessionManager.getInstance().add(session);
    }

    @Override
    public void onDestory(AbstractSession session) {
        super.onDestory(session);
        SessionManager.getInstance().remove(session);
    }
}
