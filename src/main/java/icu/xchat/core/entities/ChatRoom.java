package icu.xchat.core.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聊天室
 *
 * @author shouchen
 */
public class ChatRoom {
    private final String serverCode;
    private final ChatRoomInfo roomInfo;
    private final List<MessageInfo> messageList;

    public ChatRoom(ChatRoomInfo roomInfo, String serverCode) {
        this.roomInfo = roomInfo;
        this.serverCode = serverCode;
        this.messageList = new ArrayList<>();
    }

    public String getServerCode() {
        return serverCode;
    }

    public int getRid() {
        return roomInfo.getRid();
    }

    public ChatRoomInfo getRoomInfo() {
        return roomInfo;
    }

    public ChatRoom pushMessage(MessageInfo messageInfo) {
        synchronized (this.messageList) {
            this.messageList.add(messageInfo);
            this.messageList.sort((a, b) -> (int) (a.getTimeStamp() - b.getTimeStamp()));
        }
        return this;
    }

    public List<MessageInfo> getMessageList() {
        return Collections.unmodifiableList(messageList);
    }
}
