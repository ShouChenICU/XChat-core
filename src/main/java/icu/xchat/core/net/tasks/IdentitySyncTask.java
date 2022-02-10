package icu.xchat.core.net.tasks;

import icu.xchat.core.Identity;
import icu.xchat.core.XChatCore;
import icu.xchat.core.net.PacketBody;
import icu.xchat.core.net.Server;
import icu.xchat.core.utils.BsonUtils;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;

/**
 * 身份同步任务
 *
 * @author shouchen
 */
public class IdentitySyncTask extends AbstractTask {
    private byte[] identityData;

    public IdentitySyncTask(Server server, ProgressCallBack progressCallBack) {
        super(progressCallBack);
        this.server = server;
    }

    /**
     * 处理一个包
     *
     * @param packetBody 包
     */
    @Override
    public void handlePacket(PacketBody packetBody) throws Exception {

    }

    /**
     * 起步包
     *
     * @return 第一个发送的包
     */
    @Override
    public PacketBody startPacket() {
        BSONObject object = new BasicBSONObject();
        object.put("TIMESTAMP", XChatCore.getIdentity().getTimeStamp());
        return new PacketBody()
                .setId(this.packetCount++)
                .setData(BsonUtils.encode(object));
    }

    private byte[] encodeIdentity(Identity identity) {
        BSONObject object = new BasicBSONObject();
        object.put("ATTRIBUTES", identity.getAttributes());
        object.put("TIMESTAMP", identity.getTimeStamp());
        object.put("SIGNATURE", identity.getSignature());
        return BsonUtils.encode(object);
    }
}
