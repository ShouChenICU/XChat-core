package icu.xchat.core.net.tasks;

import icu.xchat.core.UserInfoManager;
import icu.xchat.core.callbacks.interfaces.UpdateRoomInfoCallBack;
import icu.xchat.core.callbacks.interfaces.UpdateUserInfoCallBack;
import icu.xchat.core.entities.ChatRoom;
import icu.xchat.core.entities.ChatRoomInfo;
import icu.xchat.core.entities.MessageInfo;
import icu.xchat.core.entities.UserInfo;
import icu.xchat.core.net.PacketBody;
import icu.xchat.core.net.WorkerThreadPool;
import icu.xchat.core.utils.BsonUtils;
import org.bson.BSONObject;

import java.util.Objects;

/**
 * 数据接收任务
 *
 * @author shouchen
 */
public class ReceiveTask extends AbstractTransmitTask {
    private static UpdateRoomInfoCallBack updateRoomInfoCallBack;
    private static UpdateUserInfoCallBack updateUserInfoCallBack;

    public static void setUpdateRoomInfoCallBack(UpdateRoomInfoCallBack updateRoomInfoCallBack) {
        ReceiveTask.updateRoomInfoCallBack = updateRoomInfoCallBack;
    }

    public static void setUpdateUserInfoCallBack(UpdateUserInfoCallBack updateUserInfoCallBack) {
        ReceiveTask.updateUserInfoCallBack = updateUserInfoCallBack;
    }

    public ReceiveTask() {
        super();
    }

    /**
     * 处理一个包
     *
     * @param packetBody 包
     */
    @Override
    public void handlePacket(PacketBody packetBody) {
        if (packetBody.getId() == 0) {
            BSONObject object = BsonUtils.decode(packetBody.getData());
            this.actionType = (int) object.get("ACTION_TYPE");
            this.dataType = (int) object.get("DATA_TYPE");
            this.dataContent = new byte[(int) object.get("DATA_SIZE")];
        } else if (packetBody.getId() == 1) {
            byte[] buf = packetBody.getData();
            System.arraycopy(buf, 0, dataContent, processedLength, buf.length);
            processedLength += buf.length;
        }
        if (processedLength == dataContent.length) {
            done();
        } else {
            WorkerThreadPool.execute(() -> server.postPacket(new PacketBody()
                    .setTaskId(this.taskId)
                    .setId(1)));
        }
    }

    @Override
    public void done() {
        WorkerThreadPool.execute(() -> {
            if (Objects.equals(dataType, TYPE_ROOM_INFO)) {
                ChatRoomInfo roomInfo = new ChatRoomInfo(dataContent);
                server.updateRoomInfo(roomInfo);
                updateRoomInfoCallBack.updateRoomInfo(roomInfo, server.getServerInfo().getServerCode());
            } else if (Objects.equals(dataType, TYPE_MSG_INFO)) {
                MessageInfo messageInfo = new MessageInfo(dataContent);
                ChatRoom chatRoom = server.getChatRoom(messageInfo.getRid());
                if (chatRoom != null) {
                    chatRoom.pushMessage(messageInfo);


                } else {
                    this.terminate("本地找不到对应的聊天室！");
                    server.postPacket(new PacketBody()
                            .setTaskId(this.taskId)
                            .setId(2));
                    return;
                }
            } else if (Objects.equals(dataType, TYPE_USER_INFO)) {
                UserInfo userInfo = new UserInfo(dataContent);
                UserInfoManager.putUserInfo(userInfo);
                updateUserInfoCallBack.updateUserInfo(userInfo);
            }
            server.postPacket(new PacketBody()
                    .setTaskId(this.taskId)
                    .setId(2));
            super.done();
        });
    }

    /**
     * 起步包
     *
     * @return 第一个发送的包
     */
    @Override
    public PacketBody startPacket() {
        return null;
    }
}
