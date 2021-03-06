package com.xxkun.relayserver.service;

import java.util.Map;

public interface RedisService {

    void setHashValue(String key1, String key2, String value);

    void setHashMap(String key1, Map<?, ?> map);

    void set(String key, String value, long time);

    boolean hasKey(String key);

    String get(String key);

    void remove(String key);

    void expire(String key, long time);
}
