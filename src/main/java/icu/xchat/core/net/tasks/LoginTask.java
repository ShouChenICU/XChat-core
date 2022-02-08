package icu.xchat.core.net.tasks;

import icu.xchat.core.GlobalVariables;
import icu.xchat.core.net.PacketBody;
import icu.xchat.core.utils.PayloadTypes;
import org.bson.BSONObject;
import org.bson.BasicBSONEncoder;
import org.bson.BasicBSONObject;

/**
 * 登陆任务
 *
 * @author shouchen
 */
public class LoginTask extends AbstractTask {

    public LoginTask() {
        super(null, null);
        this.packetSum = 5;
    }

    /**
     * 处理一个包
     *
     * @param packetBody 包
     * @return 下一个发送的包，为null则结束任务
     */
    @Override
    public PacketBody handlePacket(PacketBody packetBody) {
        return null;
    }

    /**
     * 起步包
     *
     * @return 第一个发送的包
     */
    @Override
    public PacketBody startPacket() {
        BSONObject object = new BasicBSONObject();
        object.put("PROTOCOL_VERSION", GlobalVariables.PROTOCOL_VERSION);
        return new PacketBody()
                .setId(this.packetCount++)
                .setPayloadType(PayloadTypes.LOGIN)
                .setData(new BasicBSONEncoder().encode(object));
    }
}
