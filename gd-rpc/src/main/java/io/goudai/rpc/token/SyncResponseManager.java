package io.goudai.rpc.token;

import io.goudai.rpc.model.Request;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by freeman on 2016/1/17.
 */
public class SyncResponseManager {
    private static final ConcurrentHashMap<String, SyncResponse> tickets = new ConcurrentHashMap<>();

    public static final SyncResponse getSyncResponse(String id) {
        if (id == null) return null;
        return tickets.get(id);
    }


    public static final SyncResponse createSyncResponse(Request req) {
        if(req == null) throw new NullPointerException("request must not be null");
        SyncResponse syncResponse = new SyncResponse(req);
        if (tickets.putIfAbsent(syncResponse.getId(), syncResponse) != null) {
            throw new IllegalArgumentException("duplicate ticket token id.");
        }
        return syncResponse;
    }

    public static final SyncResponse removeSyncResponse(String id) {
        return tickets.remove(id);
    }

}
