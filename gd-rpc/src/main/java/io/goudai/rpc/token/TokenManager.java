package io.goudai.rpc.token;

import io.goudai.rpc.model.Request;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by freeman on 2016/1/17.
 */
public class TokenManager {
    private static final ConcurrentHashMap<String, Token> tickets = new ConcurrentHashMap<>();

    public static final Token getToken(String id) {
        if (id == null) return null;
        return tickets.get(id);
    }


    public static final Token createTicket(Request req, long timeout) {
        if(req == null) throw new NullPointerException("request must not be null");
        Token token = new Token(req, timeout);
        if (tickets.putIfAbsent(token.getId(), token) != null) {
            throw new IllegalArgumentException("duplicate ticket number.");
        }
        return token;
    }

    public static final Token removeTicket(String id) {
        return tickets.remove(id);
    }

}
