package icu.xchat.core.net;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 网络核心
 *
 * @author shouchen
 */
public class NetCore {
    private static final ReadWriteLock READ_WRITE_LOCK;
    private static Selector mainSelector;

    static {
        READ_WRITE_LOCK = new ReentrantReadWriteLock();
        try {
            mainSelector = Selector.open();
            Thread thread = new Thread(NetCore::mainLoop);
            thread.setDaemon(false);
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
                mainSelector.select();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            READ_WRITE_LOCK.readLock().lock();
            try {
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();
                    if (key.isReadable()) {
                        AbstractNetIO netIO = (AbstractNetIO) key.attachment();
                        key.interestOps(0);
                        WorkerThreadPool.execute(() -> {
                            try {
                                netIO.doRead();
                            } catch (Exception e) {
                                e.printStackTrace();
                                netIO.exceptionHandler(e);
                            }
                        });
                    }
                }
            } finally {
                READ_WRITE_LOCK.readLock().unlock();
            }
        }
    }

    public static SelectionKey register(SocketChannel channel, int ops, AbstractNetIO netIO) throws ClosedChannelException {
        READ_WRITE_LOCK.writeLock().lock();
        try {
            mainSelector.wakeup();
            return channel.register(mainSelector, ops, netIO);
        } finally {
            READ_WRITE_LOCK.writeLock().unlock();
        }
    }
}
