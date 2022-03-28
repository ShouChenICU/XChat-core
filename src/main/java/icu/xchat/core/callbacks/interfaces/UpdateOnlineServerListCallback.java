package icu.xchat.core.callbacks.interfaces;

import icu.xchat.core.entities.ServerInfo;

import java.util.List;

/**
 * 在线服务器信息更新回调
 *
 * @author shouchen
 */
public interface UpdateOnlineServerListCallback {
    void updateOnlineServerList(List<ServerInfo> serverInfoList);
}
