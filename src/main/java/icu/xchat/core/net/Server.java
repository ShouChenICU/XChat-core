package icu.xchat.core.net;

import icu.xchat.core.XChatCore;
import icu.xchat.core.callbacks.interfaces.ProgressCallBack;
import icu.xchat.core.constants.TaskTypes;
import icu.xchat.core.entities.ChatRoom;
import icu.xchat.core.entities.ChatRoomInfo;
import icu.xchat.core.entities.ServerInfo;
import icu.xchat.core.exceptions.TaskException;
import icu.xchat.core.net.tasks.AbstractTask;
import icu.xchat.core.net.tasks.CommandTask;
import icu.xchat.core.net.tasks.ReceiveTask;
import icu.xchat.core.net.tasks.Task;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务器实体类
 *
 * @author shouchen
 */
public abstract class Server extends NetNode {
    private final Map<Integer, ChatRoom> roomMap;
    private final ConcurrentHashMap<Integer, Task> taskMap;
    private ConnectBootstrap connectBootstrap;
    private int taskId;

    public abstract ServerInfo getServerInfo();

    /**
     * 初始化
     */
    public Server() {
        this.roomMap = new HashMap<>();
        this.taskMap = new ConcurrentHashMap<>();
        this.taskId = 1;
    }

    /**
     * 尝试连接
     *
     * @param callBack 回调
     */
    public void connect(ProgressCallBack callBack) {
        synchronized (this) {
            if (isConnect()) {
                callBack.completeProgress();
                return;
            }
            if (connectBootstrap == null) {
                callBack.startProgress();
                try {
                    connectBootstrap = new ConnectBootstrap(this) {
                        @Override
                        protected void complete() {
                            callBack.completeProgress();
                            if (XChatCore.CallBack.updateServerStatusCallback != null) {
                                XChatCore.CallBack.updateServerStatusCallback.updateServerStatus(Server.this);
                            }
                        }

                        @Override
                        protected void exceptionHandler(Exception exception) {
                            connectBootstrap = null;
                            callBack.terminate(exception.getMessage());
                        }
                    };
                } catch (Exception e) {
                    callBack.terminate(e.toString());
                }
            } else {
                callBack.terminate("is connecting...");
            }
        }
    }

    @Override
    public void disconnect() throws Exception {
        super.disconnect();
        if (XChatCore.CallBack.updateServerStatusCallback != null) {
            XChatCore.CallBack.updateServerStatusCallback.updateServerStatus(Server.this);
        }
    }

    @Override
    public void update(AbstractNetIO abstractNetIO) {
        synchronized (this) {
            if (!isConnect()) {
                super.update(abstractNetIO);
                this.connectBootstrap = null;
                DispatchCenter.heartTest(this);
            }
        }
    }

    /**
     * 处理一个包
     *
     * @param packetBody 包
     */
    @Override
    protected void packageHandler(PacketBody packetBody) throws Exception {
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
    protected void exceptionHandler(Exception exception) {
        exception.printStackTrace();
        try {
//            disconnect();
            logout();
        } catch (Exception e) {
            e.printStackTrace();
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

    public ChatRoom getChatRoom(int rid) {
        return roomMap.get(rid);
    }

    public List<ChatRoom> getChatRoomList() {
        return new ArrayList<>(roomMap.values());
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
                chatRoom = new ChatRoom(roomInfo, getServerInfo().getServerCode());
                roomMap.put(roomInfo.getRid(), chatRoom);
            } else {
                chatRoom.setRoomInfo(roomInfo);
            }
        }
        return this;
    }

    /**
     * 注销
     */
    public void logout() throws Exception {
        postPacket(new PacketBody().setTaskType(TaskTypes.LOGOUT));
        for (Task task : taskMap.values()) {
            task.terminate("logout!");
        }
        taskMap.clear();
        taskId = 1;
        disconnect();
        ServerManager.unloadServer(this.getServerInfo().getServerCode());
    }
}
