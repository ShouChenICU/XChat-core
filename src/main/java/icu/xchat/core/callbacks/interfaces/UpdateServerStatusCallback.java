package icu.xchat.core.callbacks.interfaces;

import icu.xchat.core.net.Server;

/**
 * 服务器状态更新回调
 *
 * @author shouchen
 */
public interface UpdateServerStatusCallback {
    void updateServerStatus(Server server);
}
