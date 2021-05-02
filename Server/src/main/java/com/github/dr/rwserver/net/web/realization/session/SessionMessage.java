package com.github.dr.rwserver.net.web.realization.session;

import java.util.HashMap;
import java.util.Map;

public class SessionMessage {
    private final Map<String, Object> session = new HashMap<>();
    private int life;

    public int up() {
        life++;
        return life;
    }

    public Object get(String key) {
        return session.get(key);
    }

    public void put(String key, Object value) {
        session.put(key, value);
    }
}
