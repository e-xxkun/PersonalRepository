package com.xxkun.relayserver.service.impl;

import com.xxkun.relayserver.dao.UserIdentifier;
import com.xxkun.relayserver.dao.UserInfo;
import com.xxkun.relayserver.dao.UserSession;
import com.xxkun.relayserver.dao.mbg.model.User;
import com.xxkun.relayserver.service.RedisService;
import com.xxkun.relayserver.service.UserInfoManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserInfoManageServiceImpl implements UserInfoManageService {
    @Autowired
    private RedisService redisService;
    @Value("${redis.key.expire.EXPIRE_TIME}")
    private Long EXPIRE_TIME;

    @Override
    public boolean isUserLogin(long id) {
        return redisService.hasKey(redisService.longToString(id));
    }

    @Override
    public boolean isExistUserToken(String token) {
        return redisService.hasKey(token);
    }

    @Override
    public UserInfo setUser(User user) {
        String token = UUID.randomUUID().toString();
        String userIdStr = redisService.longToString(user.getId());
        redisService.set(token, userIdStr, EXPIRE_TIME);
        UserInfo userInfo = new UserInfo(user.getId());
        UserSession userSession = new UserSession(user.getId());
        userSession.setToken(token);
        UserIdentifier userIdentifier = new UserIdentifier();
        userInfo.setIdentifier(userIdentifier);
        userInfo.setSession(userSession);
        redisService.setHashValue(userIdStr, "IDENTIFIER", userIdentifier.toString());
        redisService.setHashValue(userIdStr, "STATE", user.getState().getCode());
        return userInfo;
    }

    @Override
    public UserSession getUserSessionFromToken(String token) {
        String idStr = redisService.get(token);
        if (idStr == null) {
            return null;
        }
        long userId = redisService.stringToLong(idStr);
        if (userId < 0) {
            return null;
        }
        UserSession session = new UserSession(userId);
        session.setToken(token);
        return session;
    }

    @Override
    public void removeUserFromSession(UserSession userSession) {
        redisService.remove(userSession.getToken());
        redisService.remove(redisService.longToString(userSession.getUserId()));
    }
}
