package icu.xchat.core;

import icu.xchat.core.callbacks.interfaces.OnlineServerListUpdateCallback;
import icu.xchat.core.callbacks.interfaces.ProgressCallBack;
import icu.xchat.core.database.DaoManager;
import icu.xchat.core.entities.ServerInfo;
import icu.xchat.core.exceptions.IdentityLoadException;
import icu.xchat.core.exceptions.TaskException;
import icu.xchat.core.net.ServerManager;
import icu.xchat.core.net.WorkerThreadPool;
import icu.xchat.core.net.tasks.IdentitySyncTask;
import icu.xchat.core.net.tasks.RoomSyncTask;

import java.io.IOException;
import java.util.List;

/**
 * XChat客户端核心
 *
 * @author shouchen
 */
@SuppressWarnings("unused")
public class XChatCore {
    private static Configuration configuration;
    private static Identity identity;

    /**
     * 获取当前配置
     *
     * @return 配置对象
     */
    public static Configuration getConfiguration() {
        return configuration;
    }

    /**
     * 初始化
     *
     * @param configuration 配置对象
     */
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

    /**
     * 获取当前身份
     *
     * @return 身份
     */
    public static Identity getIdentity() {
        return identity;
    }

    /**
     * 注销登陆
     */
    public static synchronized void logout() {
        if (XChatCore.identity != null) {
            ServerManager.closeAll();
            XChatCore.identity = null;
        }
    }

    /**
     * 服务器相关方法
     */
    public static final class Server {
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

        /**
         * 断开一个服务器连接
         *
         * @param serverCode       服务器标识码
         * @param progressCallBack 进度回调
         */
        public static void disconnectServer(String serverCode, ProgressCallBack progressCallBack) {
            progressCallBack.startProgress();
            if (ServerManager.closeServer(serverCode)) {
                progressCallBack.completeProgress();
            } else {
                progressCallBack.terminate("找不到该服务器！");
            }
        }

        /**
         * 同步身份
         *
         * @param serverCode       服务器标识码
         * @param progressCallBack 进度回调
         */
        public static void syncIdentity(String serverCode, ProgressCallBack progressCallBack) {
            try {
                ServerManager.getServerByServerCode(serverCode).addTask(
                        new IdentitySyncTask(progressCallBack)
                );
            } catch (TaskException e) {
                progressCallBack.terminate(e.getMessage());
            }
        }

        /**
         * 同步房间
         *
         * @param serverCode       服务器标识码
         * @param progressCallBack 进度回调
         */
        public static void syncRoom(String serverCode, ProgressCallBack progressCallBack) {
            try {
                ServerManager.getServerByServerCode(serverCode).addTask(
                        new RoomSyncTask(progressCallBack)
                );
            } catch (TaskException e) {
                progressCallBack.terminate(e.getMessage());
            }
        }

        /**
         * 获取在线服务器信息列表
         *
         * @return 在线服务器信息列表
         */
        public static List<ServerInfo> getOnlineServersList() {
            return ServerManager.getOnlineServersList();
        }

        /**
         * 获取服务器实体
         *
         * @param serverCode 服务器识别码
         * @return 服务器实体
         */
        public static icu.xchat.core.net.Server getServer(String serverCode) {
            return ServerManager.getServerByServerCode(serverCode);
        }
    }

    /**
     * 回调方法设置
     */
    public static final class CallBack {
        public static void setOnlineServerListUpdateCallback(OnlineServerListUpdateCallback callback) {
            ServerManager.setOnlineServerListUpdateCallback(callback);
        }
    }

    /**
     * 数据库相关设置
     */
    public static final DaoManager DAO_MANAGER = DaoManager.getInstance();
}
