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
    private static Selector mainSelector;

    static {
        try {
            mainSelector = Selector.open();
            Thread thread = new Thread(NetCore::mainLoop);
            thread.setDaemon(true);
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 主轮询
     */
    private static void mainLoop() {
        Set<SelectionKey> selectedKeys = mainSelector.selectedKeys();
        while (true) {
            try {
                mainSelector.select(128);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();
                if (key.isReadable()) {
                    Server server = (Server) key.attachment();
                    key.interestOps(0);
                    WorkerThreadPool.execute(server::doRead);
                }
            }
        }
    }

    public static void wakeup() {
        mainSelector.wakeup();
    }

    public static SelectionKey register(SocketChannel channel, int ops, NetNode netNode) throws ClosedChannelException {
        SelectionKey selectionKey = channel.register(mainSelector, ops, netNode);
        mainSelector.wakeup();
        return selectionKey;
    }
}
