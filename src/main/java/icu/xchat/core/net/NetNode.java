package icu.xchat.core.net;

import icu.xchat.core.exceptions.PacketException;
import icu.xchat.core.utils.PackageUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeoutException;

/**
 * 网络节点
 *
 * @author shouchen
 */
public abstract class NetNode {
    private final SocketChannel channel;
    private SelectionKey selectionKey;
    private final ByteBuffer readBuffer;
    private final ByteBuffer writeBuffer;
    private final PackageUtils packageUtils;
    private long heartTime;
    private int packetStatus;
    private int packetLength;
    private byte[] packetData;

    public NetNode(InetSocketAddress inetSocketAddress,int timeOut) throws IOException {
        this.channel = SocketChannel.open();
        this.channel.socket().connect(inetSocketAddress, timeOut);
        this.channel.configureBlocking(false);
        this.readBuffer = ByteBuffer.allocateDirect(512);
        this.writeBuffer = ByteBuffer.allocateDirect(512);
        this.packageUtils = new PackageUtils();
        this.packetStatus = 0;
        this.packetLength = 0;
        this.heartTime = System.currentTimeMillis();
        this.selectionKey = NetCore.register(channel, SelectionKey.OP_READ, this);
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public SelectionKey getSelectionKey() {
        return selectionKey;
    }

    public PackageUtils getPackageUtils() {
        return packageUtils;
    }

    abstract void handlePacket(PacketBody packetBody) throws Exception;

    /**
     * 接收并处理数据
     */
    public void doRead() throws Exception {
        int len;
        while ((len = channel.read(readBuffer)) != 0) {
            if (len == -1) {
                throw new IOException("通道关闭");
            }
            readBuffer.flip();
            while (readBuffer.hasRemaining()) {
                switch (packetStatus) {
                    case 0:
                        packetLength = readBuffer.get() & 0xff;
                        packetStatus = 1;
                        break;
                    case 1:
                        packetLength += (readBuffer.get() & 0xff) << 8;
                        packetData = new byte[packetLength];
                        packetLength = 0;
                        packetStatus = 2;
                        break;
                    case 2:
                        for (; readBuffer.hasRemaining() && packetLength < packetData.length; packetLength++) {
                            packetData[packetLength] = readBuffer.get();
                        }
                        if (packetLength == packetData.length) {
                            this.heartTime = System.currentTimeMillis();
                            handlePacket(packageUtils.decodePacket(packetData));
                            packetStatus = 0;
                        }
                        break;
                }
            }
            readBuffer.clear();
        }
        selectionKey = NetCore.register(channel, SelectionKey.OP_READ, this);
    }

    /**
     * 发送一个包
     *
     * @param packetBody 包
     */
    @SuppressWarnings("BusyWait")
    public void postPacket(PacketBody packetBody) throws Exception {
        synchronized (channel) {
            byte[] dat = packageUtils.encodePacket(packetBody);
            int length = dat.length;
            if (length > 65535) {
                throw new PacketException("包长度超限: " + length);
            }
            writeBuffer.put((byte) (length % 256))
                    .put((byte) (length / 256));
            int offset = 0;
            while (offset < dat.length) {
                if (writeBuffer.hasRemaining()) {
                    length = Math.min(writeBuffer.remaining(), dat.length - offset);
                    writeBuffer.put(dat, offset, length);
                    offset += length;
                }
                writeBuffer.flip();
                int waitCount = 0;
                while (writeBuffer.hasRemaining()) {
                    if (channel.write(writeBuffer) == 0) {
                        if (waitCount >= 10) {
                            throw new TimeoutException("写入超时");
                        }
                        Thread.sleep(100);
                        waitCount++;
                    } else {
                        waitCount = 0;
                    }
                }
                writeBuffer.clear();
            }
        }
        this.heartTime = System.currentTimeMillis();
    }

    public long getHeartTime() {
        return heartTime;
    }
}
