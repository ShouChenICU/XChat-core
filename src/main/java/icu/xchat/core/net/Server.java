package icu.xchat.core.net;

import icu.xchat.core.entities.ServerInfo;
import icu.xchat.core.exceptions.PacketException;
import icu.xchat.core.exceptions.TaskException;
import icu.xchat.core.net.tasks.CommandTask;
import icu.xchat.core.net.tasks.LoginTask;
import icu.xchat.core.net.tasks.ProgressCallBack;
import icu.xchat.core.net.tasks.Task;
import icu.xchat.core.utils.PackageUtils;
import icu.xchat.core.utils.TaskTypes;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * 服务器连接实体类
 *
 * @author shouchen
 */
public class Server {
    private final SocketChannel channel;
    private SelectionKey selectionKey;
    private final ByteBuffer readBuffer;
    private final ByteBuffer writeBuffer;
    private final ConcurrentHashMap<Integer, Task> taskMap;
    private final PackageUtils packageUtils;
    private final ServerInfo serverInfo;
    private long heartTime;
    private int taskId;
    private int packetStatus;
    private int packetLength;
    private byte[] packetData;

    public Server(ServerInfo serverInfo, ProgressCallBack progressCallBack) throws IOException, TaskException {
        this.serverInfo = serverInfo;
        this.channel = SocketChannel.open();
        this.channel.socket().connect(new InetSocketAddress(serverInfo.getHost(), serverInfo.getPort()), 1000);
        this.channel.configureBlocking(false);
        this.readBuffer = ByteBuffer.allocateDirect(256);
        this.writeBuffer = ByteBuffer.allocateDirect(256);
        this.taskMap = new ConcurrentHashMap<>();
        this.packageUtils = new PackageUtils();
        this.heartTime = System.currentTimeMillis();
        this.taskId = 1;
        this.packetStatus = 0;
        this.packetLength = 0;
        addTask(new LoginTask(this, progressCallBack));
        this.selectionKey = NetCore.register(channel, SelectionKey.OP_READ, this);
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

    public PackageUtils getPackageUtils() {
        return packageUtils;
    }

    /**
     * 接收并处理数据
     */
    public void doRead() {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            ServerManager.closeServer(this);
        }
    }


    /**
     * 处理一个包
     *
     * @param packetBody 包
     */
    private void handlePacket(PacketBody packetBody) throws Exception {
        if (packetBody.getTaskId() != 0) {
            Task task = taskMap.get(packetBody.getTaskId());
            if (task == null) {
                switch (packetBody.getTaskType()) {
                    case TaskTypes.COMMAND:
                        task = new CommandTask()
                                .setTaskId(packetBody.getTaskId());
                        break;
                    case TaskTypes.MSG:
                        // TODO: 2022/2/6
                        break;
                    default:
                        throw new TaskException("未知的任务类型");
                }
                taskMap.put(packetBody.getTaskId(), task);
            }
            task.handlePacket(packetBody);
        } else {

        }
    }

    /**
     * 添加一个任务
     *
     * @param task 任务
     */
    public void addTask(Task task) throws TaskException {
        PacketBody packetBody = task.startPacket();
        if (packetBody == null) {
            throw new TaskException("起步包为空");
        }
        int id;
        synchronized (taskMap) {
            id = this.taskId++;
            taskMap.put(id, task);
        }
        task.setTaskId(id);
        packetBody.setTaskId(id);
        WorkerThreadPool.execute(() -> postPacket(packetBody));
    }

    /**
     * 移除一个任务
     *
     * @param taskId 任务id
     */
    public void removeTask(int taskId) {
        this.taskMap.remove(taskId);
    }

    /**
     * 发送一个包
     *
     * @param packetBody 包
     */
    public void postPacket(PacketBody packetBody) {
        try {
            byte[] dat = packageUtils.encodePacket(packetBody);
            int length = dat.length;
            if (length > 65535) {
                throw new PacketException("包长度错误 " + length);
            }
            synchronized (channel) {
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
        } catch (Exception e) {
            e.printStackTrace();
            ServerManager.closeServer(this);
        }
    }
}
