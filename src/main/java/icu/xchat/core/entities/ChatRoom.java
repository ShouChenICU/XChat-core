package icu.xchat.core.entities;

import icu.xchat.core.callbacks.interfaces.UpdateMessageCallBack;

import java.util.*;

/**
 * 聊天室
 *
 * @author shouchen
 */
@SuppressWarnings("unused")
public class ChatRoom {
    private final String serverCode;
    private final List<MessageInfo> messageList;
    private final Set<Integer> messageSet;
    private ChatRoomInfo roomInfo;
    private int unprocessedMsgCount;
    private UpdateMessageCallBack updateMessageCallBack;

    public ChatRoom(ChatRoomInfo roomInfo, String serverCode) {
        this.roomInfo = roomInfo;
        this.serverCode = serverCode;
        this.messageList = new ArrayList<>();
        this.messageSet = new HashSet<>();
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
            if (messageSet.add(messageInfo.getId())) {
                this.messageList.add(messageInfo);
                this.messageList.sort((a, b) -> Long.compare(a.getTimeStamp() - b.getTimeStamp(), 0L));
                this.unprocessedMsgCount++;
                if (this.updateMessageCallBack != null) {
                    this.updateMessageCallBack.updateMessage(messageInfo);
                }
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
     * 获取最新消息
     *
     * @return 最新消息
     */
    public MessageInfo getLastMessage() {
        if (messageList.isEmpty()) {
            return null;
        } else {
            return messageList.get(messageList.size() - 1);
        }
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
        this.updateMessageCallBack = callBack;
        return this;
    }

    /**
     * 获取消息更新回调
     *
     * @return 回调
     */
    public UpdateMessageCallBack getUpdateMessageCallBack() {
        return updateMessageCallBack;
    }
}
