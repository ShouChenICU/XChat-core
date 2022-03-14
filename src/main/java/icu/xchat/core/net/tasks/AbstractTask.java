package icu.xchat.core.net.tasks;

import icu.xchat.core.callbacks.adapters.ProgressAdapter;
import icu.xchat.core.callbacks.interfaces.ProgressCallBack;
import icu.xchat.core.net.Server;

/**
 * 传输任务抽象类
 *
 * @author shouchen
 */
public abstract class AbstractTask implements Task {
    public static final ProgressCallBack EMPTY_PROGRESS_CALLBACK = new ProgressAdapter();
    protected Server server;
    protected int taskId;
    protected ProgressCallBack progressCallBack;

    public AbstractTask() {
        this.progressCallBack = EMPTY_PROGRESS_CALLBACK;
    }

    public AbstractTask(ProgressCallBack progressCallBack) {
        this.progressCallBack = progressCallBack;
        progressCallBack.startProgress();
    }

    public Server getServer() {
        return server;
    }

    public AbstractTask setServer(Server server) {
        this.server = server;
        return this;
    }

    @Override
    public int getTaskId() {
        return taskId;
    }

    @Override
    public AbstractTask setTaskId(int taskId) {
        this.taskId = taskId;
        return this;
    }

    @Override
    public void terminate(String errMsg) {
        progressCallBack.terminate(errMsg);
        if (server != null) {
            server.removeTask(this.taskId);
        }
    }

    public void done() {
        progressCallBack.completeProgress();
        server.removeTask(this.taskId);
    }
}
