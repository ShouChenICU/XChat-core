package icu.xchat.core.net.tasks;

import icu.xchat.core.callbacks.interfaces.ProgressCallBack;
import icu.xchat.core.constants.TaskTypes;
import icu.xchat.core.entities.Serialization;
import icu.xchat.core.net.PacketBody;
import icu.xchat.core.net.WorkerThreadPool;
import icu.xchat.core.utils.BsonUtils;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;

/**
 * 推送任务
 *
 * @author shouchen
 */
public class PushTask extends AbstractTransmitTask {

    public PushTask(Serialization obj, int dataType, int actionType, ProgressCallBack progressCallBack) {
        super(progressCallBack);
        this.actionType = actionType;
        this.dataType = dataType;
        this.dataContent = obj.serialize();
    }

    /**
     * 处理一个包
     *
     * @param packetBody 包
     */
    @Override
    public void handlePacket(PacketBody packetBody) throws Exception {
        if (packetBody.getId() == 1) {
            int len = Math.min(64000, dataContent.length - processedLength);
            byte[] buf = new byte[len];
            System.arraycopy(dataContent, processedLength, buf, 0, buf.length);
            processedLength += len;
            WorkerThreadPool.execute(() -> server.postPacket(new PacketBody()
                    .setTaskId(this.taskId)
                    .setId(1)
                    .setData(buf)));
        } else {
            done();
        }
    }

    /**
     * 起步包
     *
     * @return 第一个发送的包
     */
    @Override
    public PacketBody startPacket() {
        BSONObject object = new BasicBSONObject();
        object.put("ACTION_TYPE", this.actionType);
        object.put("DATA_TYPE", this.dataType);
        object.put("DATA_SIZE", this.dataContent.length);
        return new PacketBody()
                .setTaskType(TaskTypes.TRANSMIT)
                .setId(0)
                .setData(BsonUtils.encode(object));
    }
}
