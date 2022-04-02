package icu.xchat.core;

import icu.xchat.core.callbacks.interfaces.ProgressCallBack;
import icu.xchat.core.callbacks.interfaces.UpdateOnlineServerListCallback;
import icu.xchat.core.callbacks.interfaces.UpdateRoomInfoCallBack;
import icu.xchat.core.callbacks.interfaces.UpdateUserInfoCallBack;
import icu.xchat.core.constants.MessageTypes;
import icu.xchat.core.database.DaoManager;
import icu.xchat.core.entities.ChatRoomInfo;
import icu.xchat.core.entities.MessageInfo;
import icu.xchat.core.entities.ServerInfo;
import icu.xchat.core.exceptions.IdentityLoadException;
import icu.xchat.core.net.Server;
import icu.xchat.core.net.ServerManager;
import icu.xchat.core.net.WorkerThreadPool;
import icu.xchat.core.net.tasks.*;
import icu.xchat.core.utils.SignatureUtils;

import java.util.List;
import java.util.Objects;

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
    public static synchronized void init(Configuration configuration) {
        if (XChatCore.configuration == null) {
            XChatCore.configuration = configuration == null ? new Configuration() : configuration;
        } else {
            return;
        }
        assert configuration != null;
        WorkerThreadPool.init(configuration.getWorkerThreadCount());
        UserInfoManager.clearAll();
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
            // TODO: 2022/4/2
            XChatCore.identity = null;
        }
    }

    /**
     * 推送一个消息
     *
     * @param msg        消息文本
     * @param serverCode 服务器识别码
     * @param rid        房间id
     * @param callBack   回调
     */
    public static void pushMessage(String msg, String serverCode, int rid, ProgressCallBack callBack) {
        try {
            Server server = ServerManager.getServerByCode(serverCode);
            if (server == null) {
                callBack.terminate("Server not found");
                return;
            }
            if (msg.length() > 1024) {
                callBack.terminate("消息长度超限！");
                return;
            }
            server.addTask(new PushTask(
                    SignatureUtils.signMsg(
                            new MessageInfo()
                                    .setSender(identity.getUidCode())
                                    .setRid(rid)
                                    .setType(MessageTypes.MSG_TEXT)
                                    .setContent(msg),
                            identity.getPrivateKey()),
                    PushTask.TYPE_MSG_INFO,
                    PushTask.ACTION_CREATE,
                    callBack));
        } catch (Exception e) {
            callBack.terminate(e.getMessage());
        }
    }

    /**
     * 服务器相关方法
     */
    public static final class Servers {
        /**
         * 尝试连接一个服务器
         *
         * @param serverInfo 服务器信息
         */
        public static void attemptConnectServer(ServerInfo serverInfo, ProgressCallBack callBack) {
            try {
                Server server = ServerManager.loadServer(serverInfo);
                server.connect(callBack);
            } catch (Exception e) {
                callBack.terminate(Objects.requireNonNullElse(e.getMessage(), e.toString()));
            }
        }

        /**
         * 断开一个服务器连接
         *
         * @param serverCode 服务器标识码
         * @param callBack   进度回调
         */
        public static void disconnectServer(String serverCode, ProgressCallBack callBack) {
            callBack.startProgress();
            Server server = ServerManager.getServerByCode(serverCode);
            if (server == null) {
                callBack.terminate("Server not found");
                return;
            }
            try {
                server.disconnect();
            } catch (Exception e) {
                callBack.terminate(Objects.requireNonNullElse(e.getMessage(), e.toString()));
            }
        }

        /**
         * 测试服务器是否在线
         *
         * @param serverCode 服务器识别码
         * @return 在线状态
         */
        public static boolean isOnline(String serverCode) {
            Server server = ServerManager.getServerByCode(serverCode);
            return server != null && server.isConnect();
        }

        /**
         * 获取已加载的服务器列表
         *
         * @return 服务器列表
         */
        public static List<Server> getServerList() {
            return ServerManager.getServerList();
        }

        /**
         * 获取服务器实体
         *
         * @param serverCode 服务器识别码
         * @return 服务器实体
         */
        public static Server getServer(String serverCode) {
            return ServerManager.getServerByCode(serverCode);
        }
    }

    public static final class Tasks {

        /**
         * 同步身份
         *
         * @param serverCode 服务器标识码
         * @param callBack   进度回调
         */
        public static void syncIdentity(String serverCode, ProgressCallBack callBack) {
            try {
                Server server = ServerManager.getServerByCode(serverCode);
                if (server == null) {
                    callBack.terminate("Server not found");
                    return;
                }
                server.addTask(
                        new IdentitySyncTask(callBack)
                );
            } catch (Exception e) {
                callBack.terminate(Objects.requireNonNullElse(e.getMessage(), e.toString()));
            }
        }

        /**
         * 同步房间
         *
         * @param serverCode 服务器标识码
         * @param callBack   进度回调
         */
        public static void syncRoom(String serverCode, ProgressCallBack callBack) {
            try {
                Server server = ServerManager.getServerByCode(serverCode);
                if (server == null) {
                    callBack.terminate("Server not found");
                    return;
                }
                server.addTask(
                        new RoomSyncTask(callBack)
                );
            } catch (Exception e) {
                callBack.terminate(Objects.requireNonNullElse(e.getMessage(), e.toString()));
            }
        }

        /**
         * 同步用户信息
         *
         * @param serverCode 服务器识别码
         * @param callBack   进度回调
         */
        public static void syncUser(String serverCode, ProgressCallBack callBack) {
            try {
                Server server = ServerManager.getServerByCode(serverCode);
                if (server == null) {
                    callBack.terminate("Server not found");
                    return;
                }
                server.addTask(new UserSyncTask(callBack));
            } catch (Exception e) {
                callBack.terminate(Objects.requireNonNullElse(e.getMessage(), e.toString()));
            }
        }

        /**
         * 同步指定时间戳之前指定数量的消息
         *
         * @param serverCode 服务器识别码
         * @param rid        房间id
         * @param time       时间戳
         * @param count      数量
         * @param callBack   进度回调
         */
        public static void syncMessage(String serverCode, int rid, long time, int count, ProgressCallBack callBack) {
            try {
                Server server = ServerManager.getServerByCode(serverCode);
                if (server == null) {
                    callBack.terminate("Server not found");
                    return;
                }
                server.addTask(
                        new MessageSyncTask(rid, time, count, callBack)
                );
            } catch (Exception e) {
                callBack.terminate(Objects.requireNonNullElse(e.getMessage(), e.toString()));
            }
        }

        /**
         * 创建一个房间
         *
         * @param serverCode 服务器识别码
         * @param roomInfo   房间信息
         * @param callBack   进度回调
         */
        public static void createRoom(String serverCode, ChatRoomInfo roomInfo, ProgressCallBack callBack) {
            try {
                Server server = ServerManager.getServerByCode(serverCode);
                if (server == null) {
                    callBack.terminate("Server not found");
                    return;
                }
                server.addTask(new PushTask(roomInfo, PushTask.TYPE_ROOM_INFO, PushTask.ACTION_CREATE, callBack));
            } catch (Exception e) {
                callBack.terminate(Objects.requireNonNullElse(e.getMessage(), e.toString()));
            }
        }
    }

    /**
     * 回调设置
     */
    public static final class CallBack {
        /**
         * 服务器列表更新回调
         */
        public static UpdateOnlineServerListCallback updateOnlineServerListCallback;

        /**
         * 设置房间信息更新回调
         */
        public static UpdateRoomInfoCallBack updateRoomInfoCallBack;

        /**
         * 设置用户信息更新回调
         */
        public static UpdateUserInfoCallBack updateUserInfoCallBack;
    }

    /**
     * 数据库相关设置
     */
    public static final DaoManager DAO_MANAGER = DaoManager.getInstance();
}
