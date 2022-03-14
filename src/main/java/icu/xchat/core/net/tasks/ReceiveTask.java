package icu.xchat.core.net.tasks;

import icu.xchat.core.UserInfoManager;
import icu.xchat.core.callbacks.adapters.ReceiveAdapter;
import icu.xchat.core.callbacks.interfaces.ReceiveCallback;
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
    private static ReceiveCallback receiveCallback = new ReceiveAdapter();

    public static void setReceiveCallback(ReceiveCallback receiveCallback) {
        ReceiveTask.receiveCallback = receiveCallback;
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
                ChatRoomInfo roomInfo = new ChatRoomInfo();
                roomInfo.deserialize(dataContent);
                server.putRoom(roomInfo);
                receiveCallback.receiveRoom(roomInfo, server.getServerInfo().getServerCode());
            } else if (Objects.equals(dataType, TYPE_MSG_INFO)) {
                MessageInfo messageInfo = new MessageInfo();
                messageInfo.deserialize(dataContent);
                server.getRoom(messageInfo.getRid()).pushMessage(messageInfo);
                receiveCallback.receiveMessage(messageInfo, server.getServerInfo().getServerCode());
            } else if (Objects.equals(dataType, TYPE_USER_INFO)) {
                UserInfo userInfo = new UserInfo();
                userInfo.deserialize(dataContent);
                UserInfoManager.putUserInfo(userInfo);
                receiveCallback.receiveUser(userInfo, server.getServerInfo().getServerCode());
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
