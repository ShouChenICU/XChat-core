package icu.xchat.core;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置类
 *
 * @author shouchen
 */
public class Configuration {
    private static final String DATABASE_TYPE = "database.type";
    private static final String DATABASE_TYPE_DEFAULT = "SQLite";
    private static final String DATABASE_URL = "database.url";
    private static final String DATABASE_URL_DEFAULT = "jdbc:sqlite:xchat-core.db";
    private static final String DATABASE_USERNAME = "database.username";
    private static final String DATABASE_PASSWORD = "database.password";
    private final Map<String, String> configMap;

    public Configuration() {
        this.configMap = new HashMap<>();
    }

    public Configuration setDatabaseType(String type) {
        configMap.put(DATABASE_TYPE, type);
        return this;
    }

    public String getDatabaseType() {
        return configMap.getOrDefault(DATABASE_TYPE, DATABASE_TYPE_DEFAULT);
    }

    public Configuration setDatabaseUrl(String url) {
        configMap.put(DATABASE_URL, url);
        return this;
    }

    public String getDatabaseUrl() {
        return configMap.getOrDefault(DATABASE_URL, DATABASE_URL_DEFAULT);
    }

    public Configuration setDatabaseUsername(String username) {
        configMap.put(DATABASE_USERNAME, username);
        return this;
    }

    public String getDatabaseUsername() {
        return configMap.getOrDefault(DATABASE_USERNAME, "");
    }

    public Configuration setDatabasePassword(String password) {
        configMap.put(DATABASE_PASSWORD, password);
        return this;
    }

    public String getDatabasePassword() {
        return configMap.getOrDefault(DATABASE_PASSWORD, "");
    }
}
