package icu.xchat.core;

import icu.xchat.core.entities.UserInfo;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户管理器
 *
 * @author shouchen
 */
public final class UserInfoManager {
    private static final ConcurrentHashMap<String, UserInfo> USER_INFO_MAP;

    static {
        USER_INFO_MAP = new ConcurrentHashMap<>();
    }

    public static void putUserInfo(UserInfo userInfo) {
        USER_INFO_MAP.put(userInfo.getUidCode(), userInfo);
    }

    public static UserInfo getUserInfo(String userCode) {
        return USER_INFO_MAP.get(userCode);
    }

    public static void removeUserInfo(String uidCode) {
        USER_INFO_MAP.remove(uidCode);
    }

    public static void clearAll() {
        USER_INFO_MAP.clear();
    }
}
