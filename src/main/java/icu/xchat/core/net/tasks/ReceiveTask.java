package icu.xchat.core.net.tasks;

import icu.xchat.core.database.DaoManager;
import icu.xchat.core.entities.ChatRoomInfo;
import icu.xchat.core.net.PacketBody;
import icu.xchat.core.net.WorkerThreadPool;
import icu.xchat.core.utils.BsonUtils;
import org.bson.BSONObject;

/**
 * 数据接收任务
 *
 * @author shouchen
 */
public class ReceiveTask extends AbstractTransmitTask {
    public ReceiveTask() {
        super();
        System.out.println("receive start!");

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
            WorkerThreadPool.execute(() -> server.postPacket(new PacketBody()
                    .setTaskId(this.taskId)
                    .setId(0)));
        } else if (packetBody.getId() == 1) {
            byte[] buf = packetBody.getData();
            System.arraycopy(buf, 0, dataContent, processedLength, buf.length);
            processedLength += buf.length;
            WorkerThreadPool.execute(() -> server.postPacket(new PacketBody()
                    .setTaskId(this.taskId)
                    .setId(0)));
        } else {
            done();
        }
    }

    @Override
    public void done() {
        System.out.println("receive done!");

        if (dataType == TYPE_ROOM_INFO) {
            ChatRoomInfo roomInfo = new ChatRoomInfo();
            roomInfo.deserialize(dataContent);
            if (actionType == ACTION_CREATE) {
                DaoManager.getInstance().getRoomDao().addRoomInfo(roomInfo);
            } else if (actionType == ACTION_UPDATE) {
                DaoManager.getInstance().getRoomDao().updateRoomInfo(roomInfo);
            }
        }
        super.done();
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
