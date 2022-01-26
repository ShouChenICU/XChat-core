package icu.xchat.core;

import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * 身份管理器
 *
 * @author shouchen
 */
final class IdentityManager {
    /**
     * 从原始数据解析身份
     *
     * @param data 数据
     * @return 身份
     */
    public static Identity parseIdentity(byte[] data) {
        // TODO: 2022/1/26
        return null;
    }

    /**
     * 从文件解析身份
     *
     * @param file 文件
     * @return 身份
     */
    public static Identity parseIdentity(File file) {
        // TODO: 2022/1/26
        return null;
    }

    /**
     * 序列化身份
     *
     * @param identity 身份
     * @return 字节数组
     */
    public byte[] storeIdentity(Identity identity) {
        // TODO: 2022/1/26
        return null;
    }

    /**
     * 身份
     *
     * @author shouchen
     */
    public static class Identity {
        private String uidCode;
        private PublicKey publicKey;
        private PrivateKey privateKey;
        private Map<String, String> attributes;
        private byte[] sign;
        private long timeStamp;

        public Identity() {
            attributes = new HashMap<>();
        }
    }
}
