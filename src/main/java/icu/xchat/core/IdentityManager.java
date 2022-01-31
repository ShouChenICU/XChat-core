package icu.xchat.core;

import java.io.File;
import java.security.*;
import java.util.*;

/**
 * 身份管理器
 *
 * @author shouchen
 */
public final class IdentityManager {
    private IdentityManager() {
    }

    public static Identity genIdentity(String keypairAlgorithm, int keySize) throws NoSuchAlgorithmException {
        Identity identity = new Identity();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keypairAlgorithm);
        keyPairGenerator.initialize(keySize, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        identity.setKeyPairAlgorithm(keypairAlgorithm)
                .setPublicKey(keyPair.getPublic())
                .setPrivateKey(keyPair.getPrivate());
        return identity;
    }

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
    public byte[] encodeIdentity(Identity identity) {
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
        private String keyPairAlgorithm;
        private PublicKey publicKey;
        private PrivateKey privateKey;
        private Map<String, String> attributes;
        private long timeStamp;

        private Identity() {
            attributes = new HashMap<>();
            timeStamp = System.currentTimeMillis();
        }

        public String getUidCode() {
            return uidCode;
        }

        public Identity setKeyPairAlgorithm(String keyPairAlgorithm) {
            this.keyPairAlgorithm = keyPairAlgorithm;
            return this;
        }

        public String getKeyPairAlgorithm() {
            return keyPairAlgorithm;
        }

        public PublicKey getPublicKey() {
            return publicKey;
        }

        public Identity setPublicKey(PublicKey publicKey) throws NoSuchAlgorithmException {
            this.publicKey = publicKey;
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
            byte[] digest = messageDigest.digest(publicKey.getEncoded());
            byte[] part = new byte[12];
            System.arraycopy(digest, 0, part, 0, 12);
            this.uidCode = Base64.getEncoder().encodeToString(part);
            return this;
        }

        public PrivateKey getPrivateKey() {
            return privateKey;
        }

        public Identity setPrivateKey(PrivateKey privateKey) {
            this.privateKey = privateKey;
            return this;
        }

        public Identity setTimeStamp(long timeStamp) {
            this.timeStamp = timeStamp;
            return this;
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        public Map<String, String> getAttributes() {
            return Collections.unmodifiableMap(attributes);
        }

        public void setAttribute(String key, String value) {
            attributes.put(key, value);
            timeStamp = System.currentTimeMillis();
        }

        public void removeAttribute(String key) {
            attributes.remove(key);
            timeStamp = System.currentTimeMillis();
        }

        public String getAttribute(String key) {
            return attributes.get(key);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Identity identity = (Identity) o;
            return Objects.equals(uidCode, identity.uidCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uidCode);
        }

        @Override
        public String toString() {
            return "Identity{" +
                    "uidCode='" + uidCode + '\'' +
                    ", keyPairAlgorithm='" + keyPairAlgorithm + '\'' +
                    ", publicKeyCode=***" +
                    ", privateKeyCode=***" +
                    ", attributes=" + attributes +
                    ", timeStamp=" + timeStamp +
                    '}';
        }
    }
}
