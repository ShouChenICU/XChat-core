package icu.xchat.core;

/**
 * XChat客户端核心
 *
 * @author shouchen
 */
public class XCore {
    private static Configuration configuration;

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static void init() {
        configuration = new Configuration();
    }
}
