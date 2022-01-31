package icu.xchat.core.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.*;

/**
 * 加解密工具类
 *
 * @author shouchen
 */
public final class EncryptUtils {
    private static final int KEY_SIZE = 256;
    private static String ENCRYPT_ALGORITHM;

    public static void init(String keypairAlgorithm) {
        ENCRYPT_ALGORITHM = keypairAlgorithm;
    }

    public static SecretKey genAesKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ENCRYPT_ALGORITHM);
        keyGenerator.init(KEY_SIZE, new SecureRandom());
        return keyGenerator.generateKey();
    }

    public static Cipher getEncryptCipher(SecretKey encryptKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, encryptKey);
        return cipher;
    }

    public static Cipher getDecryptCipher(SecretKey decryptKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, decryptKey);
        return cipher;
    }

    public static Cipher getEncryptCipher(String algorithm, PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.PUBLIC_KEY, publicKey);
        return cipher;
    }

    public static Cipher getDecryptCipher(String algorithm, PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.PRIVATE_KEY, privateKey);
        return cipher;
    }
}
