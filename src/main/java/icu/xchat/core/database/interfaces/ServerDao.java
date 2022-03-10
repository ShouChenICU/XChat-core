package icu.xchat.core.database.interfaces;

import icu.xchat.core.entities.ServerInfo;

import java.util.List;

/**
 * 服务器信息数据访问接口
 *
 * @author shouchen
 */
public interface ServerDao {
    /**
     * 获取一个服务器信息
     *
     * @param serverCode 服务器识别码
     * @return 服务器信息实体
     */
    ServerInfo getServerInfo(String serverCode);

    /**
     * 获取全部服务器信息列表
     *
     * @return 服务器信息实体列表
     */
    List<ServerInfo> getServerInfoList();

    /**
     * 添加一个服务器信息
     *
     * @param serverInfo 服务器信息实体
     * @return 结果
     */
    boolean addServerInfo(ServerInfo serverInfo);

    /**
     * 更新一个服务器信息
     *
     * @param serverInfo 服务器信息实体
     * @return 结果
     */
    boolean updateServerInfo(ServerInfo serverInfo);
}
