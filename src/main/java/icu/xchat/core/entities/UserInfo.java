package icu.xchat.core.entities;

import icu.xchat.core.constants.KeyPairAlgorithms;
import icu.xchat.core.utils.BsonUtils;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * 用户信息
 *
 * @author shouchen
 */
@SuppressWarnings("unused")
public class UserInfo implements Serialization {
    /**
     * 用户唯一识别码
     */
    private String uidCode;
    /**
     * 用户属性
     */
    private Map<String, String> attributeMap;
    /**
     * 签名
     */
    private String signature;
    /**
     * 公钥
     */
    private PublicKey publicKey;
    /**
     * 修改时间
     */
    private Long timeStamp;

    public String getUidCode() {
        return uidCode;
    }

    public UserInfo setUidCode(String uidCode) {
        this.uidCode = uidCode;
        return this;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public UserInfo setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public UserInfo setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public String getSignature() {
        return signature;
    }

    public UserInfo setSignature(String signature) {
        this.signature = signature;
        return this;
    }

    public Map<String, String> getAttributeMap() {
        return Collections.unmodifiableMap(attributeMap);
    }

    public UserInfo setAttributeMap(Map<String, String> attributeMap) {
        this.attributeMap = attributeMap;
        return this;
    }

    public String getAttribute(String key) {
        return attributeMap.get(key);
    }

    public UserInfo setAttribute(String key, String value) {
        attributeMap.put(key, value);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserInfo userInfo = (UserInfo) o;
        return Objects.equals(uidCode, userInfo.uidCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uidCode);
    }

    /**
     * 对象序列化
     *
     * @return 数据
     */
    @Override
    public byte[] serialize() {
        BSONObject object = new BasicBSONObject();
        object.put("UID_CODE", uidCode);
        object.put("ATTRIBUTES", attributeMap);
        object.put("PUB_KEY", publicKey.getEncoded());
        object.put("TIME_STAMP", timeStamp);
        object.put("SIGNATURE", signature);
        return BsonUtils.encode(object);
    }

    /**
     * 反序列化为对象
     *
     * @param data 数据
     */
    @SuppressWarnings("unchecked")
    @Override
    public void deserialize(byte[] data) {
        BSONObject object = BsonUtils.decode(data);
        this.uidCode = (String) object.get("UID_CODE");
        this.attributeMap = (Map<String, String>) object.get("ATTRIBUTES");
        try {
            this.publicKey = KeyFactory.getInstance(KeyPairAlgorithms.RSA).generatePublic(new X509EncodedKeySpec((byte[]) object.get("PUB_KEY")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.timeStamp = (Long) object.get("TIME_STAMP");
        this.signature = (String) object.get("SIGNATURE");
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "uidCode='" + uidCode + '\'' +
                ", attributeMap=" + attributeMap +
                ", signature='" + signature + '\'' +
                ", publicKey=" + (publicKey == null ? "null" : "***") +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
