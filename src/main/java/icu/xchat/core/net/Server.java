package icu.xchat.core.net;

import icu.xchat.core.callbacks.interfaces.ProgressCallBack;
import icu.xchat.core.constants.TaskTypes;
import icu.xchat.core.entities.ChatRoom;
import icu.xchat.core.entities.ChatRoomInfo;
import icu.xchat.core.entities.ServerInfo;
import icu.xchat.core.exceptions.TaskException;
import icu.xchat.core.net.tasks.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务器连接实体类
 *
 * @author shouchen
 */
public class Server extends NetNode {
    private final Map<Integer, ChatRoom> roomMap;
    private final ConcurrentHashMap<Integer, Task> taskMap;
    private final ServerInfo serverInfo;
    private int taskId;

    /**
     * 初始化
     *
     * @param serverInfo            服务器信息实体对象
     * @param loginProgressCallBack 连接进度回调
     */
    public Server(ServerInfo serverInfo, ProgressCallBack loginProgressCallBack) throws Exception {
        super(new InetSocketAddress(serverInfo.getHost(), serverInfo.getPort()), 1024);
        this.serverInfo = serverInfo;
        this.roomMap = new HashMap<>();
        this.taskMap = new ConcurrentHashMap<>();
        this.taskId = 1;
        addTask(new LoginTask(loginProgressCallBack));
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    /**
     * 处理一个包
     *
     * @param packetBody 包
     */
    @Override
    protected void handlePacket(PacketBody packetBody) throws Exception {
        if (packetBody.getTaskId() != 0) {
            Task task = taskMap.get(packetBody.getTaskId());
            if (task == null) {
                switch (packetBody.getTaskType()) {
                    case TaskTypes.COMMAND:
                        task = new CommandTask()
                                .setTaskId(packetBody.getTaskId());
                        break;
                    case TaskTypes.TRANSMIT:
                        task = new ReceiveTask();
                        break;
                    default:
                        throw new TaskException("未知的任务类型");
                }
                task.setTaskId(packetBody.getTaskId());
                ((AbstractTask) task).setServer(this);
                taskMap.put(packetBody.getTaskId(), task);
            }
            if (!Objects.equals(packetBody.getTaskType(), TaskTypes.ERROR)) {
                task.handlePacket(packetBody);
            } else {
                task.terminate(new String(packetBody.getData(), StandardCharsets.UTF_8));
            }
        } else {
            throw new Exception("task id = 0");
        }
    }

    @Override
    public void postPacket(PacketBody packetBody) {
        try {
            super.postPacket(packetBody);
        } catch (Exception e) {
            e.printStackTrace();
            ServerManager.closeServer(this);
        }
    }

    @Override
    public void doRead() {
        try {
            super.doRead();
        } catch (Exception e) {
            e.printStackTrace();
            ServerManager.closeServer(this);
        }
    }

    /**
     * 添加一个任务
     *
     * @param task 任务
     */
    public void addTask(Task task) throws TaskException {
        ((AbstractTask) task).setServer(this);
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
     * 获取已加载的房间列表
     *
     * @return 房间列表
     */
    public List<Integer> getRidList() {
        return new ArrayList<>(roomMap.keySet());
    }

    public ChatRoom getChatRoom(int rid) {
        return roomMap.get(rid);
    }

    /**
     * 更新房间信息
     *
     * @param roomInfo 房间信息
     */
    public Server updateRoomInfo(ChatRoomInfo roomInfo) {
        synchronized (roomMap) {
            ChatRoom chatRoom = roomMap.get(roomInfo.getRid());
            if (chatRoom == null) {
                chatRoom = new ChatRoom(roomInfo, serverInfo.getServerCode());
                roomMap.put(roomInfo.getRid(), chatRoom);
            } else {
                chatRoom.setRoomInfo(roomInfo);
            }
        }
        return this;
    }

    @Override
    public void close() throws IOException {
        super.close();
        for (Task task : taskMap.values()) {
            task.terminate("Closed!");
        }
    }
}
