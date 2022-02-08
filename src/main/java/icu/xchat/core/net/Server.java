package icu.xchat.core.net;

import icu.xchat.core.entities.ServerInfo;
import icu.xchat.core.exceptions.UnknowTaskException;
import icu.xchat.core.net.tasks.CommandTask;
import icu.xchat.core.net.tasks.Task;
import icu.xchat.core.utils.PackageUtils;
import icu.xchat.core.utils.PayloadTypes;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务器连接实体类
 *
 * @author shouchen
 */
public class Server {
    private final SocketChannel channel;
    private SelectionKey selectionKey;
    private final ByteBuffer readBuffer;
    private final ConcurrentHashMap<Integer, Task> taskMap;
    private final PackageUtils packageUtils;
    private final ServerInfo serverInfo;
    private int taskId;
    private int packetStatus;
    private int packetLength;
    private byte[] packetData;

    public Server(ServerInfo serverInfo) throws IOException {
        this.serverInfo = serverInfo;
        this.channel = SocketChannel.open();
        this.channel.socket().connect(new InetSocketAddress(serverInfo.getHost(), serverInfo.getPort()), 1000);
        this.readBuffer = ByteBuffer.allocateDirect(256);
        this.taskMap = new ConcurrentHashMap<>();
        this.packageUtils = new PackageUtils();
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public SelectionKey getSelectionKey() {
        return selectionKey;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

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
     * 处理一个包
     *
     * @param packetBody 包
     */
    private void handlePacket(PacketBody packetBody) throws Exception {
        PacketBody packet;
        if (packetBody.getTaskId() != 0) {
            Task task = taskMap.get(packetBody.getTaskId());
            if (task == null) {
                switch (packetBody.getPayloadType()) {
                    case PayloadTypes.COMMAND:
                        task = new CommandTask();
                        break;
                    case PayloadTypes.MSG:
                        // TODO: 2022/2/6
                        break;
                    default:
                        throw new UnknowTaskException();
                }
                taskMap.put(packetBody.getTaskId(), task);
            }
            packet = task.handlePacket(packetBody);
            if (packet != null) {
                packet.setTaskId(packetBody.getTaskId());
                postPacket(packet);
            } else {
                taskMap.remove(packetBody.getTaskId());
            }
        } else {

        }
    }

    public void postPacket(PacketBody packetBody) {

    }
}
