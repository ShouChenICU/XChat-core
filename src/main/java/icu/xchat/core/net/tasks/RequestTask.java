package icu.xchat.core.net.tasks;

import icu.xchat.core.callbacks.interfaces.ProgressCallBack;
import icu.xchat.core.net.PacketBody;

/**
 * 请求资源任务
 *
 * @author shouchen
 */
public class RequestTask extends AbstractTransmitTask {

    public RequestTask(ProgressCallBack progressCallBack) {
        super(progressCallBack);
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
        return null;
    }
}
