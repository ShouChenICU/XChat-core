package icu.xchat.core.net.tasks;

import icu.xchat.core.net.PacketBody;

/**
 * 推送任务
 *
 * @author shouchen
 */
public class PushTask extends AbstractTransmitTask {
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
        return null;
    }
}
