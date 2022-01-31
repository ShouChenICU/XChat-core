package icu.xchat.core.utils;

import icu.xchat.core.net.PacketBody;
import org.bson.*;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.DataFormatException;

/**
 * 网络传输帧工具类
 *
 * @author shouchen
 */
public class PackageUtils {
    private final BSONEncoder encoder;
    private final BSONDecoder decoder;
    private Cipher encryptCipher;
    private Cipher decryptCipher;

    public PackageUtils() {
        this.encoder = new BasicBSONEncoder();
        this.decoder = new BasicBSONDecoder();
    }

    public PackageUtils setEncryptKey(SecretKey encryptKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        this.encryptCipher = EncryptUtils.getEncryptCipher(encryptKey);
        this.decryptCipher = EncryptUtils.getDecryptCipher(encryptKey);
        return this;
    }

    public PackageUtils setEncryptCipher(Cipher encryptCipher) {
        this.encryptCipher = encryptCipher;
        return this;
    }

    public PackageUtils setDecryptCipher(Cipher decryptCipher) {
        this.decryptCipher = decryptCipher;
        return this;
    }

    public byte[] encodePacket(PacketBody packetBody) throws IllegalBlockSizeException, BadPaddingException {
        BSONObject object = new BasicBSONObject();
        object.put("TASK_ID", packetBody.getTaskId());
        object.put("ID", packetBody.getId());
        object.put("PAYLOAD_TYPE", packetBody.getPayloadType());
        object.put("DATA", packetBody.getData());
        byte[] data = CompressionUtils.compress(encoder.encode(object));
        if (encryptCipher != null) {
            data = encryptCipher.doFinal(data);
        }
        return data;
    }

    public PacketBody decodePacket(byte[] data) throws IllegalBlockSizeException, BadPaddingException, DataFormatException {
        if (decryptCipher != null) {
            data = decryptCipher.doFinal(data);
        }
        data = CompressionUtils.deCompress(data);
        BSONObject object = decoder.readObject(data);
        return new PacketBody()
                .setTaskId((Integer) object.get("TASK_ID"))
                .setId((Integer) object.get("ID"))
                .setPayloadType(((Integer) object.get("PAYLOAD_TYPE")))
                .setData((byte[]) object.get("DATA"));
    }
}