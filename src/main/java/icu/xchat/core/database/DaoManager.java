package icu.xchat.core.database;

import icu.xchat.core.database.interfaces.UserInfoDao;

/**
 * 数据库访问对象管理器
 *
 * @author shouchen
 */
public class DaoManager {
    private static UserInfoDao userInfoDao;

    public static void setUserInfoDao(UserInfoDao userInfoDao) {
        DaoManager.userInfoDao = userInfoDao;
    }

    public static UserInfoDao getUserInfoDao() {
        return userInfoDao;
    }
}
