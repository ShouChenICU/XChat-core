package icu.xchat.core.net;

import icu.xchat.core.constants.TaskTypes;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 调度中心
 *
 * @author shouchen
 */
public class DispatchCenter {
    private static final ScheduledThreadPoolExecutor TIMER_EXECUTOR;

    static {
        TIMER_EXECUTOR = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * 心跳检测
     *
     * @param server 服务端实体
     */
    public static void heartTest(Server server) {
        if (server.isConnect()) {
            if (System.currentTimeMillis() - server.getHeartTime() > TimeUnit.SECONDS.toMillis(10)) {
                WorkerThreadPool.execute(() -> server.postPacket(new PacketBody().setTaskType(TaskTypes.HEART)));
            }
            TIMER_EXECUTOR.schedule(() -> heartTest(server), 10, TimeUnit.SECONDS);
        }
    }
}
