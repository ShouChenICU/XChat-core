package icu.xchat.core.net.tasks;

import icu.xchat.core.Identity;
import icu.xchat.core.XChatCore;
import icu.xchat.core.callbacks.interfaces.ProgressCallBack;
import icu.xchat.core.net.PacketBody;
import icu.xchat.core.net.WorkerThreadPool;
import icu.xchat.core.utils.BsonUtils;
import icu.xchat.core.utils.TaskTypes;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * 身份同步任务
 *
 * @author shouchen
 */
public class IdentitySyncTask extends AbstractTask {
    private boolean isUpload;
    private byte[] identityData;
    private int processedSize;

    public IdentitySyncTask(ProgressCallBack progressCallBack) {
        super(progressCallBack);
    }

    /**
     * 处理一个包
     *
     * @param packetBody 包
     */
    @Override
    public void handlePacket(PacketBody packetBody) {
        if (Objects.equals(packetBody.getTaskType(), TaskTypes.ERROR)) {
            terminate(new String(packetBody.getData(), StandardCharsets.UTF_8));
            return;
        }
        if (packetBody.getId() == 0) {
            if (packetBody.getData()[0] == 1) {
                /*
                 * 用户 -> 服务器
                 */
                isUpload = true;
                this.identityData = encodeIdentity(XChatCore.getIdentity());
                this.processedSize = 0;
                BSONObject object = new BasicBSONObject();
                object.put("SIZE", this.identityData.length);
                server.postPacket(new PacketBody()
                        .setTaskId(this.taskId)
                        .setId(1)
                        .setData(BsonUtils.encode(object)));
                upload();
            } else if (packetBody.getData()[0] == 0) {
                /*
                 * 服务器 -> 用户
                 */
                isUpload = false;
                this.processedSize = 0;
            } else {
                /*
                 * 不同步
                 */
                done();
            }
        } else if (packetBody.getId() == 1) {
            int size = (int) BsonUtils.decode(packetBody.getData()).get("SIZE");
            this.identityData = new byte[size];
            this.processedSize = 0;
        } else if (isUpload) {
            upload();
        } else {
            download(packetBody);
        }
    }

    private void upload() {
        this.progressCallBack.updateProgress((double) processedSize / identityData.length);
        byte[] buf;
        int pendingSize = identityData.length - processedSize;
        if (pendingSize > 64000) {
            buf = new byte[64000];
            System.arraycopy(identityData, processedSize, buf, 0, buf.length);
            processedSize += buf.length;
            WorkerThreadPool.execute(() -> server.postPacket(new PacketBody()
                    .setTaskId(this.taskId)
                    .setId(2)
                    .setData(buf)));
        } else {
            buf = new byte[pendingSize];
            System.arraycopy(identityData, processedSize, buf, 0, buf.length);
            processedSize += buf.length;
            WorkerThreadPool.execute(() -> server.postPacket(new PacketBody()
                    .setTaskId(this.taskId)
                    .setId(2)
                    .setData(buf)));
            done();
        }
    }

    private void download(PacketBody packetBody) {
        byte[] buf = packetBody.getData();
        System.arraycopy(buf, 0, this.identityData, this.processedSize, buf.length);
        processedSize += buf.length;
        this.progressCallBack.updateProgress((double) processedSize / identityData.length);
        if (processedSize == identityData.length) {
            done();
        } else {
            WorkerThreadPool.execute(() -> server.postPacket(new PacketBody()
                    .setTaskId(this.taskId)));
        }
    }

    /**
     * 起步包
     *
     * @return 第一个发送的包
     */
    @Override
    public PacketBody startPacket() {
        this.progressCallBack.updateProgress(0);
        BSONObject object = new BasicBSONObject();
        object.put("TIMESTAMP", XChatCore.getIdentity().getTimeStamp());
        object.put("PUBLIC_KEY", XChatCore.getIdentity().getPublicKey().getEncoded());
        return new PacketBody()
                .setId(this.packetCount = 0)
                .setTaskType(TaskTypes.IDENTITY_SYNC)
                .setData(BsonUtils.encode(object));
    }

    private byte[] encodeIdentity(Identity identity) {
        BSONObject object = new BasicBSONObject();
        object.put("ATTRIBUTES", identity.getAttributes());
        object.put("TIMESTAMP", identity.getTimeStamp());
        object.put("SIGNATURE", identity.getSignature());
        return BsonUtils.encode(object);
    }

    @Override
    public void terminate(String errMsg) {
        super.terminate(errMsg);
        server.removeTask(this.taskId);
    }

    @SuppressWarnings({"all"})
    @Override
    public void done() {
        if (!isUpload) {
            BSONObject object = BsonUtils.decode(this.identityData);
            Identity tempIdentity = new Identity()
                    .setAttributes((Map<String, String>) object.get("ATTRIBUTES"))
                    .setTimeStamp((Long) object.get("TIMESTAMP"))
                    .setSignature((String) object.get("SIGNATURE"))
                    .setPublicKey(XChatCore.getIdentity().getPublicKey());
            if (tempIdentity.checkSignature()) {
                Identity identity = XChatCore.getIdentity();
                synchronized (identity) {
                    identity.setAttributes(tempIdentity.getAttributes())
                            .setTimeStamp(tempIdentity.getTimeStamp())
                            .setSignature(tempIdentity.getSignature());
                }
            } else {
                this.progressCallBack.terminate("身份签名认证失败");
            }
        }
        super.done();
        server.removeTask(this.taskId);
    }
}
