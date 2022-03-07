package icu.xchat.core.net;

import icu.xchat.core.callbacks.interfaces.OnlineServerListUpdateCallback;
import icu.xchat.core.callbacks.interfaces.ProgressCallBack;
import icu.xchat.core.entities.ServerInfo;
import icu.xchat.core.exceptions.TaskException;
import icu.xchat.core.constants.TaskTypes;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 服务器管理器
 *
 * @author shouchen
 */
public final class ServerManager {
    private static final ScheduledThreadPoolExecutor TIMER_EXECUTOR;
    private static final Map<String, Server> onlineServersMap;
    private static OnlineServerListUpdateCallback onlineServerListUpdateCallback;

    static {
        TIMER_EXECUTOR = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });
        onlineServersMap = new HashMap<>();
        onlineServerListUpdateCallback = list -> {
        };
    }

    /**
     * 尝试连接到一个服务器
     *
     * @param serverInfo 服务器信息
     * @throws IOException 连接失败
     */
    public static void connectServer(ServerInfo serverInfo, ProgressCallBack progressCallBack) throws IOException, TaskException {
        synchronized (onlineServersMap) {
            if (onlineServersMap.containsKey(serverInfo.getServerCode())) {
                progressCallBack.terminate("不可重复连接服务器！");
                return;
            }
            Server server = new Server(serverInfo, progressCallBack);
            onlineServersMap.put(serverInfo.getServerCode(), server);
            heartTest(server);
            onlineServerListUpdateCallback.onlineServerListUpdate(getOnlineServersList());
        }
    }

    /**
     * 心跳检测
     *
     * @param server 服务端实体
     */
    private static void heartTest(Server server) {
        if (server.getChannel().isConnected()) {
            if (System.currentTimeMillis() - server.getHeartTime() > 10000) {
                WorkerThreadPool.execute(() -> server.postPacket(new PacketBody().setTaskType(TaskTypes.HEART)));
            }
            TIMER_EXECUTOR.schedule(() -> heartTest(server), 10, TimeUnit.SECONDS);
        }
    }

    /**
     * 关闭一个服务器
     *
     * @param server 服务器实体
     */
    public static void closeServer(Server server) {
        if (server == null) {
            return;
        }
        boolean isOnline = true;
        synchronized (onlineServersMap) {
            if (onlineServersMap.containsKey(server.getServerInfo().getServerCode())) {
                onlineServersMap.remove(server.getServerInfo().getServerCode());
                onlineServerListUpdateCallback.onlineServerListUpdate(getOnlineServersList());
            } else {
                isOnline = false;
            }
        }
        server.getSelectionKey().cancel();
        NetCore.wakeup();
        if (isOnline) {
            server.postPacket(new PacketBody()
                    .setTaskId(0)
                    .setTaskType(TaskTypes.LOGOUT));
        }
        try {
            server.getChannel().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭一个Server
     *
     * @param serverCode 服务器标识码
     */
    public static boolean closeServer(String serverCode) {
        Server server;
        synchronized (onlineServersMap) {
            if (onlineServersMap.containsKey(serverCode)) {
                server = onlineServersMap.remove(serverCode);
                onlineServerListUpdateCallback.onlineServerListUpdate(getOnlineServersList());
            } else {
                return false;
            }
        }
        server.getSelectionKey().cancel();
        NetCore.wakeup();
        server.postPacket(new PacketBody()
                .setTaskId(0)
                .setTaskType(TaskTypes.LOGOUT));
        try {
            server.getChannel().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 获取在线服务器信息列表
     *
     * @return 在线服务器信息列表
     */
    public static List<ServerInfo> getOnlineServersList() {
        List<ServerInfo> serverInfoList = new ArrayList<>();
        synchronized (onlineServersMap) {
            for (Map.Entry<String, Server> entry : onlineServersMap.entrySet()) {
                serverInfoList.add(entry.getValue().getServerInfo());
            }
        }
        return serverInfoList;
    }

    /**
     * 获取服务器实体
     *
     * @param serverCode 服务器标识码
     * @return 服务器实体
     */
    public static Server getServerByServerCode(String serverCode) {
        return onlineServersMap.get(serverCode);
    }

    public static void closeAll() {
        synchronized (onlineServersMap) {
            Iterator<Map.Entry<String, Server>> iterator = onlineServersMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Server server = iterator.next().getValue();
                server.getSelectionKey().cancel();
                NetCore.wakeup();
                server.postPacket(new PacketBody()
                        .setTaskId(0)
                        .setTaskType(TaskTypes.LOGOUT));
                try {
                    server.getChannel().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                iterator.remove();
            }
        }
    }

    public static void setOnlineServerListUpdateCallback(OnlineServerListUpdateCallback onlineServerListUpdateCallback) {
        ServerManager.onlineServerListUpdateCallback = onlineServerListUpdateCallback;
    }
}
