package icu.xchat.core.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * 加解密工具类
 *
 * @author shouchen
 */
@SuppressWarnings("unused")
public final class EncryptUtils {
    private static final int KEY_SIZE = 256;
    private static final int T_LEN = 128;
    private static final String ENCRYPT_KEY_ALGORITHM = "AES";
    private static final String ENCRYPT_ALGORITHM = "AES/GCM/NoPadding";

    public static SecretKey genAesKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ENCRYPT_KEY_ALGORITHM);
        keyGenerator.init(KEY_SIZE, new SecureRandom());
        return keyGenerator.generateKey();
    }

    public static byte[] genIV() {
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[16];
        random.nextBytes(iv);
        return iv;
    }

    public static byte[] genIV(String passwd) throws NoSuchAlgorithmException {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(passwd.getBytes(StandardCharsets.UTF_8));
        byte[] iv = new byte[16];
        System.arraycopy(digest, 0, iv, 0, iv.length);
        return iv;
    }

    public static SecretKey genAesKey(String password) throws Exception {
        return new SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(password.getBytes(StandardCharsets.UTF_8)), ENCRYPT_KEY_ALGORITHM);
    }

    public static Cipher getEncryptCipher(SecretKey encryptKey, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, encryptKey, new GCMParameterSpec(T_LEN, iv));
        return cipher;
    }

    public static Cipher getDecryptCipher(SecretKey decryptKey, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, decryptKey, new GCMParameterSpec(T_LEN, iv));
        return cipher;
    }

    public static Cipher getEncryptCipher(PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.PUBLIC_KEY, publicKey);
        return cipher;
    }

    public static Cipher getDecryptCipher(PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.PRIVATE_KEY, privateKey);
        return cipher;
    }

    public static PublicKey getPublicKey(String algorithm, byte[] encode) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        return keyFactory.generatePublic(new X509EncodedKeySpec(encode));
    }

    public static PrivateKey getPrivateKey(String algorithm, byte[] encode) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encode));
    }
}
