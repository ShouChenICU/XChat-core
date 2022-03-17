package icu.xchat.core.net.tasks;

import icu.xchat.core.callbacks.interfaces.ProgressCallBack;
import icu.xchat.core.constants.TaskTypes;
import icu.xchat.core.net.PacketBody;
import icu.xchat.core.utils.BsonUtils;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;

/**
 * 房间同步任务
 *
 * @author shouchen
 */
public class MessageSyncTask extends AbstractTask {
    private final int rid;
    private final long time;
    private final int count;

    public MessageSyncTask(int rid, long time, int count, ProgressCallBack callBack) {
        super(callBack);
        this.rid = rid;
        this.time = time;
        this.count = count;
    }

    /**
     * 处理一个包
     *
     * @param packetBody 包
     */
    @Override
    public void handlePacket(PacketBody packetBody) throws Exception {
        done();
    }

    /**
     * 起步包
     *
     * @return 第一个发送的包
     */
    @Override
    public PacketBody startPacket() {
        BSONObject object = new BasicBSONObject();
        object.put("RID", rid);
        object.put("TIME", time);
        object.put("COUNT", count);
        return new PacketBody()
                .setId(0)
                .setTaskType(TaskTypes.MSG_SYNC)
                .setData(BsonUtils.encode(object));
    }
}
