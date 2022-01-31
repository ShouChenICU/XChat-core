package icu.xchat.core.net;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * 服务器连接实体类
 *
 * @author shouchen
 */
public class Server {
    private SocketChannel channel;
    private SelectionKey selectionKey;

    public Server(SocketChannel channel) {
        this.channel = channel;
    }

    public void doRead() {

    }
}
