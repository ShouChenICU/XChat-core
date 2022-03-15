package icu.xchat.core.net.tasks;

import icu.xchat.core.Identity;
import icu.xchat.core.UserInfoManager;
import icu.xchat.core.XChatCore;
import icu.xchat.core.callbacks.interfaces.ProgressCallBack;
import icu.xchat.core.constants.TaskTypes;
import icu.xchat.core.entities.UserInfo;
import icu.xchat.core.net.PacketBody;

/**
 * 用户信息同步
 *
 * @author shouchen
 */
public class UserSyncTask extends AbstractTask {

    public UserSyncTask(ProgressCallBack callBack) {
        super(callBack);
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

    @Override
    public void done() {
        Identity identity = XChatCore.getIdentity();
        UserInfoManager.putUserInfo(new UserInfo()
                .setUidCode(identity.getUidCode())
                .setAttributeMap(identity.getAttributes())
                .setPublicKey(identity.getPublicKey())
                .setSignature(identity.getSignature())
                .setTimeStamp(identity.getTimeStamp()));
        super.done();
    }

    /**
     * 起步包
     *
     * @return 第一个发送的包
     */
    @Override
    public PacketBody startPacket() {
        return new PacketBody()
                .setTaskType(TaskTypes.USER_SYNC)
                .setId(0);
    }
}
