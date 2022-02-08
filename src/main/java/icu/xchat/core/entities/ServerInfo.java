package icu.xchat.core.entities;

import java.util.Objects;

/**
 * 服务器信息类
 *
 * @author shouchen
 */
public class ServerInfo {
    private String serverCode;
    private String akeAlgorithm;
    private String host;
    private Integer port;

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
