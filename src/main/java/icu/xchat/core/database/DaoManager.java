package icu.xchat.core.database;

import icu.xchat.core.database.interfaces.ServerDao;

/**
 * 数据库访问对象管理器
 *
 * @author shouchen
 */
@SuppressWarnings("unused")
public class DaoManager {
    private static volatile DaoManager daoManager;
    private ServerDao serverDao;

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

    public DaoManager setServerDao(ServerDao serverDao) {
        this.serverDao = serverDao;
        return this;
    }

    public ServerDao getServerDao() {
        return serverDao;
    }
}
