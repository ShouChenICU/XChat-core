package icu.xchat.core.database;

import icu.xchat.core.database.interfaces.UserDao;

/**
 * 数据库访问对象管理器
 *
 * @author shouchen
 */
@SuppressWarnings("unused")
public class DaoManager {
    private static volatile DaoManager daoManager;
    private UserDao userDao;

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

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public UserDao getUserDao() {
        return userDao;
    }
}
