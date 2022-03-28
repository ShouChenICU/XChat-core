package icu.xchat.core.entities;

import icu.xchat.core.callbacks.interfaces.UpdateMessageCallBack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聊天室
 *
 * @author shouchen
 */
@SuppressWarnings("unused")
public class ChatRoom {
    private final String serverCode;
    private final List<MessageInfo> messageList;
    private ChatRoomInfo roomInfo;
    private int unprocessedMsgCount;
    private UpdateMessageCallBack callBack;

    public ChatRoom(ChatRoomInfo roomInfo, String serverCode) {
        this.roomInfo = roomInfo;
        this.serverCode = serverCode;
        this.messageList = new ArrayList<>();
        this.unprocessedMsgCount = 0;
    }

    /**
     * 获取服务器识别码
     *
     * @return 服务器识别码
     */
    public String getServerCode() {
        return serverCode;
    }

    /**
     * 获取房间id
     *
     * @return 房间id
     */
    public int getRid() {
        return roomInfo.getRid();
    }

    /**
     * 获取房间信息
     *
     * @return 房间信息实体
     */
    public ChatRoomInfo getRoomInfo() {
        return roomInfo;
    }

    /**
     * 设置房间信息
     *
     * @param roomInfo 房间信息实体
     */
    public ChatRoom setRoomInfo(ChatRoomInfo roomInfo) {
        this.roomInfo = roomInfo;
        return this;
    }

    /**
     * 推送消息
     *
     * @param messageInfo 消息
     */
    public ChatRoom pushMessage(MessageInfo messageInfo) {
        synchronized (this.messageList) {
            this.messageList.add(messageInfo);
            this.messageList.sort((a, b) -> Long.compare(a.getTimeStamp() - b.getTimeStamp(), 0L));
            this.unprocessedMsgCount++;
            if (this.callBack != null) {
                this.callBack.updateMessage(messageInfo, serverCode);
            }
        }
        return this;
    }

    /**
     * 获取消息列表
     *
     * @return 消息列表
     */
    public List<MessageInfo> getMessageList() {
        return Collections.unmodifiableList(messageList);
    }

    /**
     * 获取未读消息数
     *
     * @return 未读消息数
     */
    public int getUnprocessedMsgCount() {
        return unprocessedMsgCount;
    }

    /**
     * 设置未读消息数
     *
     * @param unprocessedMsgCount 未读消息数
     */
    public ChatRoom setUnprocessedMsgCount(int unprocessedMsgCount) {
        this.unprocessedMsgCount = unprocessedMsgCount;
        return this;
    }

    /**
     * 设置消息更新回调
     *
     * @param callBack 回调
     */
    public ChatRoom setUpdateMessageCallBack(UpdateMessageCallBack callBack) {
        this.callBack = callBack;
        return this;
    }
}
