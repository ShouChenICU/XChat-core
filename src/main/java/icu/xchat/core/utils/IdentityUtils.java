package icu.xchat.core.utils;

import icu.xchat.core.Identity;
import icu.xchat.core.constants.KeyPairAlgorithms;
import org.bson.BSONObject;
import org.bson.BasicBSONDecoder;
import org.bson.BasicBSONEncoder;
import org.bson.BasicBSONObject;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

/**
 * 身份管理器
 *
 * @author shouchen
 */
@SuppressWarnings("unused")
public final class IdentityUtils {
    private IdentityUtils() {
    }

    /**
     * 生成一个新身份
     */
    public static icu.xchat.core.Identity genIdentity(String keypairAlgorithm, int keySize) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keypairAlgorithm);
        keyPairGenerator.initialize(keySize, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        String uidCode = getCodeByPublicKeyCode(keyPair.getPublic().getEncoded());
        return new Identity()
                .setUidCode(uidCode)
                .setPublicKey(keyPair.getPublic())
                .setPrivateKey(keyPair.getPrivate())
                .sign();
    }

    /**
     * 从原始数据解析身份
     *
     * @param data     身份数据
     * @param password 密码
     * @return 身份
     */
    @SuppressWarnings("unchecked")
    public static Identity parseIdentity(byte[] data, String password) throws Exception {
        Cipher cipher = EncryptUtils.getDecryptCipher(EncryptUtils.genAesKey(password), EncryptUtils.genIV(password));
        data = cipher.doFinal(data);
        data = CompressionUtils.deCompress(data);
        BSONObject object = new BasicBSONDecoder().readObject(data);
        KeyFactory keyFactory = KeyFactory.getInstance(KeyPairAlgorithms.RSA);
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec((byte[]) object.get("PUBLIC_KEY")));
        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec((byte[]) object.get("PRIVATE_KEY")));
        return new Identity()
                .setUidCode((String) object.get("UID_CODE"))
                .setPublicKey(publicKey)
                .setPrivateKey(privateKey)
                .setAttributes((Map<String, String>) object.get("ATTRIBUTES"))
                .setTimeStamp((long) object.get("TIMESTAMP"))
                .setSignature((String) object.get("SIGNATURE"));
    }

    /**
     * 从文件解析身份
     *
     * @param file     身份文件
     * @param password 密码
     * @return 身份
     */
    public static Identity parseIdentity(File file, String password) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (FileInputStream inputStream = new FileInputStream(file)) {
            int len;
            byte[] buf = new byte[256];
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
        }
        return parseIdentity(outputStream.toByteArray(), password);
    }

    /**
     * 序列化身份
     *
     * @param identity 身份
     * @param password 密码
     * @return 字节数组
     */
    public static byte[] encodeIdentity(Identity identity, String password) throws Exception {
        BSONObject object = new BasicBSONObject();
        object.put("UID_CODE", identity.getUidCode());
        object.put("PUBLIC_KEY", identity.getPublicKey().getEncoded());
        object.put("PRIVATE_KEY", identity.getPrivateKey().getEncoded());
        object.put("ATTRIBUTES", identity.getAttributes());
        object.put("TIMESTAMP", identity.getTimeStamp());
        object.put("SIGNATURE", identity.getSignature());
        byte[] dat = new BasicBSONEncoder().encode(object);
        dat = CompressionUtils.compress(dat);
        Cipher cipher = EncryptUtils.getEncryptCipher(EncryptUtils.genAesKey(password), EncryptUtils.genIV(password));
        return cipher.doFinal(dat);
    }

    /**
     * 存储身份到文件
     *
     * @param identity  身份
     * @param password  密码
     * @param storeFile 存储文件
     */
    public static void storeIdentity(Identity identity, String password, File storeFile) throws Exception {
        byte[] dat = encodeIdentity(identity, password);
        try (FileOutputStream outputStream = new FileOutputStream(storeFile)) {
            outputStream.write(dat);
            outputStream.flush();
        }
    }

    /**
     * 从公钥计算标识码
     *
     * @param publicKeyCode 公钥
     * @return 标识码
     */
    public static String getCodeByPublicKeyCode(byte[] publicKeyCode) throws Exception {
        byte[] digestCode;
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        digestCode = messageDigest.digest(publicKeyCode);
        byte[] buf = new byte[12];
        System.arraycopy(digestCode, 0, buf, 0, buf.length);
        return Base64.getEncoder().encodeToString(buf);
    }
}
