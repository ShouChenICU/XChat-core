package icu.xchat.core.net.tasks;

import icu.xchat.core.GlobalVariables;
import icu.xchat.core.XChatCore;
import icu.xchat.core.net.PacketBody;
import icu.xchat.core.net.Server;
import icu.xchat.core.net.WorkerThreadPool;
import icu.xchat.core.utils.EncryptUtils;
import icu.xchat.core.utils.KeyPairAlgorithms;
import icu.xchat.core.utils.PayloadTypes;
import org.bson.BSONObject;
import org.bson.BasicBSONEncoder;
import org.bson.BasicBSONObject;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.security.PublicKey;

/**
 * 登陆任务
 *
 * @author shouchen
 */
public class LoginTask extends AbstractTask {
    private final Server server;

    public LoginTask(Server server) {
        super(null, null);
        this.server = server;
        this.packetSum = 5;
    }

    /**
     * 处理一个包
     *
     * @param packetBody 包
     */
    @Override
    public void handlePacket(PacketBody packetBody) throws Exception {
        byte[] data = packetBody.getData();
        switch (packetBody.getId()) {
            case 0:
                PublicKey publicKey = EncryptUtils.getPublicKey(KeyPairAlgorithms.RSA, data);
                server.getPackageUtils().setEncryptCipher(EncryptUtils.getEncryptCipher(KeyPairAlgorithms.RSA, publicKey));
                SecretKey aesKey = EncryptUtils.genAesKey();
                server.postPacket(new PacketBody()
                        .setId(packetCount++)
                        .setTaskId(this.taskId)
                        .setData(aesKey.getEncoded()));
                server.getPackageUtils().setEncryptKey(aesKey);
                break;
            case 1:
                WorkerThreadPool.execute(() -> server.postPacket(new PacketBody()
                        .setTaskId(this.taskId)
                        .setId(packetCount++)
                        .setData(XChatCore.getIdentity().getPublicKey().getEncoded())));
                break;
            case 2:
                Cipher cipher = EncryptUtils.getDecryptCipher(KeyPairAlgorithms.RSA, XChatCore.getIdentity().getPrivateKey());
                byte[] dat = cipher.doFinal(data);
                WorkerThreadPool.execute(() -> server.postPacket(new PacketBody()
                        .setTaskId(this.taskId)
                        .setId(packetCount++)
                        .setData(dat)));
                done();
                break;
        }
    }

    @Override
    public void done() {
        server.removeTask(this.taskId);
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
