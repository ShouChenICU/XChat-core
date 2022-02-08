package icu.xchat.core.net;

import icu.xchat.core.entities.ServerInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务器管理器
 *
 * @author shouchen
 */
public final class ServerManager {
    private static final Map<String, Server> onlineServersMap;

    static {
        onlineServersMap = new HashMap<>();
    }

    /**
     * 尝试连接到一个服务器
     *
     * @param serverInfo 服务器信息
     * @throws IOException 连接失败
     */
    public static void connectServer(ServerInfo serverInfo) throws IOException {
        synchronized (onlineServersMap) {
            if (onlineServersMap.containsKey(serverInfo.getServerCode())) {
                return;
            }
            Server server = new Server(serverInfo);
            onlineServersMap.put(serverInfo.getServerCode(), server);
        }
    }

    public static void closeServer(Server server) {
        if (server == null) {
            return;
        }
        synchronized (onlineServersMap) {
            onlineServersMap.remove(server.getServerInfo().getServerCode());
        }
        server.getSelectionKey().cancel();
        NetCore.wakeup();
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
    public static void closeServer(String serverCode) {
        Server server;
        synchronized (onlineServersMap) {
            if (onlineServersMap.containsKey(serverCode)) {
                server = onlineServersMap.remove(serverCode);
            } else {
                return;
            }
        }
        server.getSelectionKey().cancel();
        NetCore.wakeup();
        try {
            server.getChannel().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public static void closeAll() {

    }
}
