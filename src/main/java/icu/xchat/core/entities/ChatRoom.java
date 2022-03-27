package icu.xchat.core.entities;

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

    public ChatRoom setRoomInfo(ChatRoomInfo roomInfo) {
        this.roomInfo = roomInfo;
        return this;
    }

    public ChatRoom pushMessage(MessageInfo messageInfo) {
        synchronized (this.messageList) {
            this.messageList.add(messageInfo);
            this.messageList.sort((a, b) -> Long.compare(a.getTimeStamp() - b.getTimeStamp(), 0L));
        }
        return this;
    }

    public List<MessageInfo> getMessageList() {
        return Collections.unmodifiableList(messageList);
    }
}
