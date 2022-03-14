package icu.xchat.core.callbacks.adapters;

import icu.xchat.core.callbacks.interfaces.ReceiveCallback;
import icu.xchat.core.entities.ChatRoomInfo;
import icu.xchat.core.entities.MessageInfo;
import icu.xchat.core.entities.UserInfo;

/**
 * 接收实体回调适配器
 *
 * @author shouchen
 */
public class ReceiveAdapter implements ReceiveCallback {
    /**
     * 收到一个房间信息
     *
     * @param roomInfo   房间信息
     * @param serverCode 服务器识别码
     */
    @Override
    public void receiveRoom(ChatRoomInfo roomInfo, String serverCode) {
    }

    /**
     * 收到一个消息
     *
     * @param messageInfo 消息实体
     * @param serverCode  服务器识别码
     */
    @Override
    public void receiveMessage(MessageInfo messageInfo, String serverCode) {
    }

    /**
     * 收到一个用户信息
     *
     * @param userInfo   用户信息实体
     * @param serverCode 服务器识别码
     */
    @Override
    public void receiveUser(UserInfo userInfo, String serverCode) {
    }
}
