package icu.xchat.core.net;

import icu.xchat.core.callbacks.interfaces.ProgressCallBack;
import icu.xchat.core.entities.ServerInfo;
import icu.xchat.core.exceptions.TaskException;
import icu.xchat.core.net.tasks.AbstractTask;
import icu.xchat.core.net.tasks.CommandTask;
import icu.xchat.core.net.tasks.LoginTask;
import icu.xchat.core.net.tasks.Task;
import icu.xchat.core.utils.TaskTypes;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务器连接实体类
 *
 * @author shouchen
 */
public class Server extends NetNode {
    private final ConcurrentHashMap<Integer, Task> taskMap;
    private final ServerInfo serverInfo;
    private int taskId;

    /**
     * 初始化
     *
     * @param serverInfo            服务器信息实体对象
     * @param loginProgressCallBack 连接进度回调
     */
    public Server(ServerInfo serverInfo, ProgressCallBack loginProgressCallBack) throws IOException, TaskException {
        super(new InetSocketAddress(serverInfo.getHost(), serverInfo.getPort()), 1024);
        this.serverInfo = serverInfo;
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
}
