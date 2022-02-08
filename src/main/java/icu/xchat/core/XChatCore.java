package icu.xchat.core;

import icu.xchat.core.net.NetCore;
import icu.xchat.core.net.ServerManager;

import java.io.IOException;

/**
 * XChat客户端核心
 *
 * @author shouchen
 */
public class XChatCore {
    private static Configuration configuration;
    private static Identity identity;

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static void init(Configuration configuration) throws IOException {
        XChatCore.configuration = configuration == null ? new Configuration() : configuration;
    }

    /**
     * 加载一个身份
     *
     * @param identity 身份
     */
    public static synchronized void loadIdentity(Identity identity) {
        if (XChatCore.identity == null) {
            XChatCore.identity = identity;
        }
    }

    public static synchronized void logout() {
        if (XChatCore.identity != null) {
            ServerManager.closeAll();
            XChatCore.identity = null;
        }
    }
}
