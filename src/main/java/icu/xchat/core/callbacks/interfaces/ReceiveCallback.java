package icu.xchat.core.callbacks.interfaces;

import icu.xchat.core.UserInfoManager;
import icu.xchat.core.entities.ChatRoomInfo;
import icu.xchat.core.entities.MessageInfo;
import icu.xchat.core.entities.UserInfo;

/**
 * 实体接收回调接口
 *
 * @author shouchen
 */
public interface ReceiveCallback {
    /**
     * 收到一个房间信息
     *
     * @param roomInfo   房间信息
     * @param serverCode 服务器识别码
     */
    void receiveRoom(ChatRoomInfo roomInfo, String serverCode);

    /**
     * 收到一个消息
     *
     * @param messageInfo 消息实体
     * @param serverCode  服务器识别码
     */
    void receiveMessage(MessageInfo messageInfo, String serverCode);

    /**
     * 收到一个用户信息
     *
     * @param userInfo   用户信息实体
     * @param serverCode 服务器识别码
     */
    void receiveUser(UserInfo userInfo, String serverCode);
}
