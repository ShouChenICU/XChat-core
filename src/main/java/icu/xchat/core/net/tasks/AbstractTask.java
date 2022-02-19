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
    protected int packetCount;
    protected ProgressCallBack progressCallBack;

    public AbstractTask() {
        this.packetCount = 0;
        this.progressCallBack = EMPTY_PROGRESS_CALLBACK;
    }

    public AbstractTask(ProgressCallBack progressCallBack) {
        this.packetCount = 0;
        this.progressCallBack = progressCallBack;
        progressCallBack.startProgress();
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
    }

    public void done() {
        progressCallBack.completeProgress();
    }
}
