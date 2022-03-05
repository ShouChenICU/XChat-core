package icu.xchat.core.entities;

import java.util.Map;

/**
 * 用户信息
 *
 * @author shouchen
 */
@SuppressWarnings("unused")
public class UserInfo {
    /**
     * 用户唯一识别码
     */
    private String uidCode;
    /**
     * 用户属性
     */
    private Map<String, String> attributes;
    /**
     * 签名
     */
    private String signature;
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

    public String getSignature() {
        return signature;
    }

    public UserInfo setSignature(String signature) {
        this.signature = signature;
        return this;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public UserInfo setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
        return this;
    }
}
