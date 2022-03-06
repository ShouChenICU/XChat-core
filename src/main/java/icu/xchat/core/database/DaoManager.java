package icu.xchat.core.database;

import icu.xchat.core.database.interfaces.UserInfoDao;

/**
 * 数据库访问对象管理器
 *
 * @author shouchen
 */
@SuppressWarnings("unused")
public class DaoManager {
    private static volatile DaoManager daoManager;
    private UserInfoDao userInfoDao;

    public static DaoManager getInstance() {
        if (daoManager == null) {
            synchronized (DaoManager.class) {
                if (daoManager == null) {
                    daoManager = new DaoManager();
                }
            }
        }
        return daoManager;
    }

    private DaoManager() {
    }

    public void setUserInfoDao(UserInfoDao userInfoDao) {
        this.userInfoDao = userInfoDao;
    }

    public UserInfoDao getUserInfoDao() {
        return userInfoDao;
    }
}
