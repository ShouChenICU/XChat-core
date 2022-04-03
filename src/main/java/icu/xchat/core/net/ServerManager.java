package icu.xchat.core.net;

import icu.xchat.core.entities.ServerInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 服务器管理器
 *
 * @author shouchen
 */
public final class ServerManager {
    private static final Map<String, Server> SERVER_MAP;
    private static final ReadWriteLock READ_WRITE_LOCK;

    static {
        SERVER_MAP = new HashMap<>();
        READ_WRITE_LOCK = new ReentrantReadWriteLock();
    }

    /**
     * 加载服务器
     *
     * @param serverInfo 服务器信息
     * @return 服务器实体
     */
    public static Server loadServer(ServerInfo serverInfo) throws Exception {
        if (serverInfo.getServerCode() == null || serverInfo.getHost() == null || serverInfo.getPort() == null) {
            throw new Exception("server info broken");
        }
        READ_WRITE_LOCK.writeLock().lock();
        try {
            Server server = getServerByCode(serverInfo.getServerCode());
            if (server == null) {
                server = new Server() {
                    @Override
                    public ServerInfo getServerInfo() {
                        return serverInfo;
                    }
                };
                SERVER_MAP.put(serverInfo.getServerCode(), server);
            }
            return server;
        } finally {
            READ_WRITE_LOCK.writeLock().unlock();
        }
    }

    /**
     * 获取服务器实体
     *
     * @param serverCode 服务器识别码
     * @return 服务器实体
     */
    public static Server getServerByCode(String serverCode) {
        READ_WRITE_LOCK.readLock().lock();
        try {
            return SERVER_MAP.get(serverCode);
        } finally {
            READ_WRITE_LOCK.readLock().unlock();
        }
    }

    /**
     * 获取已加载的服务器列表
     *
     * @return 服务器列表
     */
    public static List<Server> getServerList() {
        READ_WRITE_LOCK.readLock().lock();
        try {
            return new ArrayList<>(SERVER_MAP.values());
        } finally {
            READ_WRITE_LOCK.readLock().unlock();
        }
    }

    /**
     * 卸载服务器
     *
     * @param serverCode 服务器识别码
     */
    public static void unloadServer(String serverCode) {
        READ_WRITE_LOCK.writeLock().lock();
        try {
            SERVER_MAP.remove(serverCode);
        } finally {
            READ_WRITE_LOCK.writeLock().unlock();
        }
    }

    /**
     * 登出全部服务器
     */
    public static void logoutAll() {
        READ_WRITE_LOCK.readLock().lock();
        try {
            for (Server server : SERVER_MAP.values()) {
                try {
                    server.logout();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } finally {
            READ_WRITE_LOCK.readLock().unlock();
        }
    }
}
