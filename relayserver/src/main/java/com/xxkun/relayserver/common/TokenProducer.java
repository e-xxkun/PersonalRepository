package com.xxkun.relayserver.common;

import org.springframework.beans.factory.annotation.Value;

import java.util.UUID;

public class TokenProducer {
    @Value("common.token.length")
    private static Integer TOKEN_LENGTH = 16;

    public static String get(long userId) {
        String token = UUID.randomUUID().toString();
//        token = token.substring(0, TOKEN_LENGTH - Long.BYTES / Character.BYTES) + Utils.longToString(userId);
        token = token.substring(0, TOKEN_LENGTH);
        return token;
    }
}
