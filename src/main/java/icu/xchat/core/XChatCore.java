package icu.xchat.core;

import icu.xchat.core.callbacks.interfaces.OnlineServerListUpdateCallback;
import icu.xchat.core.database.DaoManager;
import icu.xchat.core.database.interfaces.UserInfoDao;
import icu.xchat.core.entities.ServerInfo;
import icu.xchat.core.exceptions.IdentityLoadException;
import icu.xchat.core.exceptions.TaskException;
import icu.xchat.core.net.ServerManager;
import icu.xchat.core.net.WorkerThreadPool;
import icu.xchat.core.callbacks.interfaces.ProgressCallBack;

import java.io.IOException;

/**
 * XChat客户端核心
 *
 * @author shouchen
 */
public class XChatCore {
    private static Configuration configuration;
    private static Identity identity;

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static void init(Configuration configuration) {
        XChatCore.configuration = configuration;
        if (XChatCore.configuration == null) {
            XChatCore.configuration = new Configuration();
        }
        assert configuration != null;
        WorkerThreadPool.init(configuration.getWorkerThreadCount());
        Runtime.getRuntime().addShutdownHook(new Thread(XChatCore::logout));
    }

    /**
     * 加载一个身份
     *
     * @param identity 身份
     */
    public static synchronized void loadIdentity(Identity identity) throws IdentityLoadException {
        if (XChatCore.identity == null) {
            XChatCore.identity = identity;
        } else {
            throw new IdentityLoadException("重复加载身份");
        }
    }

    public static Identity getIdentity() {
        return identity;
    }

    public static synchronized void logout() {
        if (XChatCore.identity != null) {
            ServerManager.closeAll();
            XChatCore.identity = null;
        }
    }

    /**
     * 服务器相关方法
     */
    public static class Server {
        /**
         * 尝试连接一个服务器
         *
         * @param serverInfo 服务器信息
         */
        public static void attemptConnectServer(ServerInfo serverInfo, ProgressCallBack progressCallBack) {
            WorkerThreadPool.execute(() -> {
                try {
                    ServerManager.connectServer(serverInfo, progressCallBack);
                } catch (IOException | TaskException e) {
                    progressCallBack.terminate(e.getMessage());
                }
            });
        }
    }

    /**
     * 回调方法设置
     */
    public static class CallBack {
        public static void setOnlineServerListUpdateCallback(OnlineServerListUpdateCallback callback) {
            ServerManager.setOnlineServerListUpdateCallback(callback);
        }
    }

    /**
     * 数据库相关设置
     */
    public static class Database {
        public static void setUserInfoDao(UserInfoDao userInfoDao) {
            DaoManager.setUserInfoDao(userInfoDao);
        }
    }
}
