package icu.xchat.core.entities;

import java.util.Objects;

/**
 * 服务器信息类
 *
 * @author shouchen
 */
@SuppressWarnings("unused")
public class ServerInfo {
    /**
     * 服务器标识码
     */
    private String serverCode;
    /**
     * 密钥交换算法
     */
    private String akeAlgorithm;
    /**
     * 服务器地址
     */
    private String host;
    /**
     * 服务器端口
     */
    private Integer port;
    /**
     * 标签
     */
    private Object tag;

    public String getServerCode() {
        return serverCode;
    }

    public ServerInfo setServerCode(String serverCode) {
        this.serverCode = serverCode;
        return this;
    }

    public String getHost() {
        return host;
    }

    public ServerInfo setHost(String host) {
        this.host = host;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public ServerInfo setPort(Integer port) {
        this.port = port;
        return this;
    }

    public String getAkeAlgorithm() {
        return akeAlgorithm;
    }

    public ServerInfo setAkeAlgorithm(String akeAlgorithm) {
        this.akeAlgorithm = akeAlgorithm;
        return this;
    }

    public Object getTag() {
        return tag;
    }

    public ServerInfo setTag(Object tag) {
        this.tag = tag;
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
        ServerInfo that = (ServerInfo) o;
        return Objects.equals(serverCode, that.serverCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverCode);
    }

    @Override
    public String toString() {
        return "ServerInfo{" +
                "serverCode='" + serverCode + '\'' +
                ", akeAlgorithm='" + akeAlgorithm + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
