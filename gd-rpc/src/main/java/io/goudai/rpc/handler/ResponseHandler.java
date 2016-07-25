package io.goudai.rpc.handler;

import io.goudai.net.handler.ChannelHandler;
import io.goudai.net.session.AbstractSession;
import io.goudai.rpc.model.Heartbeat;
import io.goudai.rpc.model.Response;
import io.goudai.rpc.token.SyncResponse;
import io.goudai.rpc.token.SyncResponseManager;

/**
 * Created by freeman on 2016/1/17.
 * 处理服务端返回的响应
 */
public class ResponseHandler implements ChannelHandler {
    @Override
    public void received(AbstractSession session, Object object) {
        if (object instanceof Response) {
            Response response = (Response) object;
            SyncResponse ticket = SyncResponseManager.removeSyncResponse(response.getId());
            if (ticket != null) {
                ticket.notifyResponse(response);
            }
        } else if (object instanceof Heartbeat) {
            // ignore
        }

    }
}
