package icu.xchat.core.net;

import icu.xchat.core.GlobalVariables;
import icu.xchat.core.XChatCore;
import icu.xchat.core.constants.KeyPairAlgorithms;
import icu.xchat.core.entities.ServerInfo;
import icu.xchat.core.utils.BsonUtils;
import icu.xchat.core.utils.EncryptUtils;
import icu.xchat.core.utils.Encryptor;
import icu.xchat.core.utils.IdentityUtils;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;

/**
 * 连接引导器
 *
 * @author shouchen
 */
public abstract class ConnectBootstrap extends AbstractNetIO {
    private static final int TIME_OUT = 1024;
    private final Server server;
    private final Encryptor encryptor;
    private int status;

    public ConnectBootstrap(Server server) throws Exception {
        super(true);
        if (server.isConnect()) {
            throw new Exception("The server is currently connected");
        }
        this.server = server;
        this.encryptor = new Encryptor();
        this.status = 0;
        ServerInfo serverInfo = server.getServerInfo();
        SocketChannel channel = SocketChannel.open();
        channel.socket().connect(new InetSocketAddress(serverInfo.getHost(), serverInfo.getPort()), TIME_OUT);
        channel.configureBlocking(false);
        key = NetCore.register(channel, SelectionKey.OP_READ, this);
        BSONObject object = new BasicBSONObject();
        object.put("PROTOCOL", GlobalVariables.PROTOCOL_VERSION);
        doWrite(BsonUtils.encode(object));
    }

    @Override
    protected void dataHandler(byte[] data) throws Exception {
        BSONObject object;
        if (status == 0) {
            object = BsonUtils.decode(data);
            // 密钥协商
            // 解析服务端公钥
            byte[] pubKeyCode = (byte[]) object.get("PUB_KEY");
            String serverCode = IdentityUtils.getCodeByPublicKeyCode(pubKeyCode);
            if (!Objects.equals(serverCode, server.getServerInfo().getServerCode())) {
                throw new Exception("Server public key error");
            }
            KeyFactory keyFactory = KeyFactory.getInstance(KeyPairAlgorithms.RSA);
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(pubKeyCode));
            Cipher cipher = EncryptUtils.getEncryptCipher(publicKey);
            // 生成密钥和IV信息
            SecretKey aesKey = EncryptUtils.genAesKey();
            byte[] encryptIV = EncryptUtils.genIV();
            byte[] decryptIV = EncryptUtils.genIV();
            // 初始化加解密器
            encryptor.initCrypto(aesKey, encryptIV, decryptIV);
            object = new BasicBSONObject();
            object.put("KEY", aesKey.getEncoded());
            object.put("ENCRYPT_IV", encryptIV);
            object.put("DECRYPT_IV", decryptIV);
            byte[] dat = BsonUtils.encode(object);
            // 用服务端公钥加密
            dat = cipher.doFinal(dat);
            doWrite(dat);
            // 发送身份识别码
            object = new BasicBSONObject();
            object.put("UID_CODE", XChatCore.getIdentity().getUidCode());
            dat = BsonUtils.encode(object);
            dat = encryptor.encode(dat);
            doWrite(dat);
            status = 1;
        } else if (status == 1) {
            System.out.println("===========1");

            data = encryptor.decode(data);
            object = BsonUtils.decode(data);
            if (!(boolean) object.get("STATUS")) {
                throw new Exception(new String((byte[]) object.get("CONTENT")));
            }
            System.out.println("===========2");
            Cipher cipher = EncryptUtils.getDecryptCipher(XChatCore.getIdentity().getPrivateKey());
            byte[] authCode = cipher.doFinal((byte[]) object.get("CONTENT"));
            object = new BasicBSONObject();
            object.put("AUTH_CODE", authCode);
            doWrite(encryptor.encode(BsonUtils.encode(object)));
            status = 2;
        } else if (status == 2) {
            data = encryptor.decode(data);
            object = BsonUtils.decode(data);
            // 验证失败
            if (!(boolean) object.get("STATUS")) {
                throw new Exception(new String((byte[]) object.get("CONTENT")));
            }
            // 验证成功
            key.attach(server);
            server.updateEncryptor(encryptor);
            server.update(this);
            complete();
        } else {
            throw new Exception("");
        }
    }

    protected abstract void complete();
}
