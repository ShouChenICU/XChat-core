package icu.xchat.core.net;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * 网络核心
 *
 * @author shouchen
 */
public class NetCore {
    private static volatile NetCore netCore;
    private Selector mainSelector;

    public static NetCore getInstance() throws IOException {
        if (netCore == null) {
            synchronized (NetCore.class) {
                if (netCore == null) {
                    netCore = new NetCore();
                }
            }
        }
        return netCore;
    }

    private NetCore() throws IOException {
        mainSelector = Selector.open();

    }

    /**
     * 主轮询
     */
    private void mainLoop() throws IOException {
        Set<SelectionKey> selectedKeys = mainSelector.selectedKeys();
        while (true) {
            mainSelector.select();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();
                if (key.isReadable()) {
                    Server server = (Server) key.attachment();
                    key.cancel();
                    WorkerThreadPool.execute(server::doRead);
                }
            }
        }
    }

    public SelectionKey register(SocketChannel channel, int ops, Server server) throws ClosedChannelException {
        SelectionKey selectionKey = channel.register(mainSelector, ops, server);
        mainSelector.wakeup();
        return selectionKey;
    }
}
