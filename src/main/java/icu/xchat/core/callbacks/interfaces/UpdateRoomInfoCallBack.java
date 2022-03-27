package icu.xchat.core.callbacks.interfaces;

import icu.xchat.core.entities.ChatRoomInfo;

/**
 * 聊天室信息更新回调
 *
 * @author shouchen
 */
public interface UpdateRoomInfoCallBack {
    /**
     * 更新聊天室信息
     *
     * @param roomInfo   聊天室信息实体
     * @param serverCode 服务器识别码
     */
    void updateRoomInfo(ChatRoomInfo roomInfo, String serverCode);
}
