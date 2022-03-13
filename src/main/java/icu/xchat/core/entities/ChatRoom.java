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
    private ChatRoomInfo roomInfo;
    private List<MessageInfo> messageList;

    public ChatRoom(ChatRoomInfo roomInfo) {
        this.roomInfo = roomInfo;
        this.messageList = new ArrayList<>();
    }

    public int getRid() {
        return roomInfo.getRid();
    }

    public ChatRoomInfo getRoomInfo() {
        return roomInfo;
    }

    // TODO: 2022/3/13  

    public List<MessageInfo> getMessageList() {
        return Collections.unmodifiableList(messageList);
    }
}
