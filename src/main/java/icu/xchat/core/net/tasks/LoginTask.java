package icu.xchat.core.net.tasks;

import icu.xchat.core.GlobalVariables;
import icu.xchat.core.XChatCore;
import icu.xchat.core.callbacks.interfaces.ProgressCallBack;
import icu.xchat.core.net.PacketBody;
import icu.xchat.core.net.WorkerThreadPool;
import icu.xchat.core.utils.BsonUtils;
import icu.xchat.core.utils.EncryptUtils;
import icu.xchat.core.utils.KeyPairAlgorithms;
import icu.xchat.core.utils.TaskTypes;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.security.auth.login.LoginException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Objects;

/**
 * 登陆任务
 *
 * @author shouchenthis
 */
public class LoginTask extends AbstractTask {
    public LoginTask(ProgressCallBack progressCallBack) {
        super(progressCallBack);
    }

    /**
     * 处理一个包
     *
     * @param packetBody 包
     */
    @Override
    public void handlePacket(PacketBody packetBody) throws Exception {
        if (Objects.equals(packetBody.getTaskType(), TaskTypes.ERROR)) {
            this.terminate(new String(packetBody.getData(), StandardCharsets.UTF_8));
            return;
        }
        byte[] data = packetBody.getData();
        switch (packetBody.getId()) {
            case 0:
                this.packetCount = 1;
                this.progressCallBack.updateProgress(0.25);
                /*
                 * 获取并验证服务器公钥
                 */
                byte[] digest = MessageDigest.getInstance("SHA-256").digest(data);
                byte[] tmp = new byte[12];
                System.arraycopy(digest, 0, tmp, 0, tmp.length);
                if (!Objects.equals(server.getServerInfo().getServerCode(), Base64.getEncoder().encodeToString(tmp))) {
                    terminate("服务器信息验证失败");
                    throw new LoginException("服务器信息验证失败");
                }
                PublicKey publicKey = EncryptUtils.getPublicKey(KeyPairAlgorithms.RSA, data);
                server.getPackageUtils().setEncryptCipher(EncryptUtils.getEncryptCipher(KeyPairAlgorithms.RSA, publicKey));
                /*
                 * 生成对称密钥
                 */
                BSONObject object = new BasicBSONObject();
                SecretKey aesKey = EncryptUtils.genAesKey();
                object.put("KEY", aesKey.getEncoded());
                byte[] decryptIV = EncryptUtils.genIV();
                object.put("ENCRYPT_IV", decryptIV);
                byte[] encryptIV = EncryptUtils.genIV();
                object.put("DECRYPT_IV", encryptIV);
                server.postPacket(new PacketBody()
                        .setId(packetCount)
                        .setTaskId(this.taskId)
                        .setData(BsonUtils.encode(object)));
                server.getPackageUtils().initCrypto(aesKey, encryptIV, decryptIV);
                break;
            case 1:
                this.packetCount = 2;
                this.progressCallBack.updateProgress(0.5);
                /*
                 * 发送自己的公钥
                 */
                WorkerThreadPool.execute(() -> server.postPacket(new PacketBody()
                        .setTaskId(this.taskId)
                        .setId(this.packetCount)
                        .setData(XChatCore.getIdentity().getPublicKey().getEncoded())));
                break;
            case 2:
                this.packetCount = 3;
                this.progressCallBack.updateProgress(0.75);
                Cipher cipher = EncryptUtils.getDecryptCipher(KeyPairAlgorithms.RSA, XChatCore.getIdentity().getPrivateKey());
                byte[] dat = cipher.doFinal(data);
                WorkerThreadPool.execute(() -> server.postPacket(new PacketBody()
                        .setTaskId(this.taskId)
                        .setId(packetCount)
                        .setData(dat)));
                break;
            case 3:
                done();
                break;
        }
    }

    @Override
    public void terminate(String errMsg) {
        super.terminate(errMsg);
        server.removeTask(this.taskId);
    }

    @Override
    public void done() {
        super.done();
        server.removeTask(this.taskId);
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
        object.put("PROTOCOL_VERSION", GlobalVariables.PROTOCOL_VERSION);
        return new PacketBody()
                .setId(this.packetCount = 0)
                .setTaskType(TaskTypes.LOGIN)
                .setData(BsonUtils.encode(object));
    }
}
