package icu.xchat.core.net.tasks;

import icu.xchat.core.callbacks.interfaces.ProgressCallBack;
import icu.xchat.core.net.PacketBody;

/**
 * 请求资源任务
 *
 * @author shouchen
 */
public class RequestTask extends AbstractTransmitTask {

    public RequestTask(int type, int action, ProgressCallBack progressCallBack) {
        // TODO: 2022/3/17
    }

    /**
     * 处理一个包
     *
     * @param packetBody 包
     */
    @Override
    public void handlePacket(PacketBody packetBody) throws Exception {
        // TODO: 2022/3/17
    }

    /**
     * 起步包
     *
     * @return 第一个发送的包
     */
    @Override
    public PacketBody startPacket() {
        // TODO: 2022/3/17
        return null;
    }
}
