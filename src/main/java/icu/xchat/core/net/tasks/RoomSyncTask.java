package icu.xchat.core.net.tasks;

import icu.xchat.core.callbacks.interfaces.ProgressCallBack;
import icu.xchat.core.constants.TaskTypes;
import icu.xchat.core.entities.ChatRoom;
import icu.xchat.core.net.PacketBody;
import icu.xchat.core.net.WorkerThreadPool;
import icu.xchat.core.utils.BsonUtils;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

/**
 * 房间信息同步任务
 *
 * @author shouchen
 */
public class RoomSyncTask extends AbstractTask {
    private List<Integer> ridList;

    public RoomSyncTask(ProgressCallBack progressCallBack) {
        super(progressCallBack);
    }

    /**
     * 处理一个包
     *
     * @param packetBody 包
     */
    @SuppressWarnings("unchecked")
    @Override
    public void handlePacket(PacketBody packetBody) {
        if (Objects.equals(packetBody.getId(), 0)) {
            BSONObject object;
            object = BsonUtils.decode(packetBody.getData());
            ridList = (List<Integer>) object.get("RID_LIST");
            if (!ridList.isEmpty()) {
                sendItem();
            }
        } else if (Objects.equals(packetBody.getId(), 1)) {
            if (!ridList.isEmpty()) {
                sendItem();
            }
        } else {
            done();
        }
    }

    private void sendItem() {
        int rid = ridList.remove(0);
        WorkerThreadPool.execute(() -> {
            ChatRoom chatRoom = server.getChatRoom(rid);
            try {
                byte[] hash;
                if (chatRoom != null) {
                    hash = MessageDigest.getInstance("SHA-256").digest(chatRoom.getRoomInfo().serialize());
                } else {
                    hash = "null".getBytes(StandardCharsets.UTF_8);
                }
                BSONObject bsonObject = new BasicBSONObject();
                bsonObject.put("RID", rid);
                bsonObject.put("HASH", hash);
                server.postPacket(new PacketBody()
                        .setTaskId(this.taskId)
                        .setId(1)
                        .setData(BsonUtils.encode(bsonObject)));
            } catch (NoSuchAlgorithmException e) {
                terminate(e.getMessage());
            }
        });
    }

    /**
     * 起步包
     *
     * @return 第一个发送的包
     */
    @Override
    public PacketBody startPacket() {
        return new PacketBody()
                .setId(0)
                .setTaskType(TaskTypes.ROOM_SYNC);
    }
}
