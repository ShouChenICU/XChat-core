package icu.xchat.core.net;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 任务线程池
 *
 * @author shouchen
 */
public class WorkerThreadPool {
    private static final int MAX_WORK_COUNT = 4096;
    private static final String THREAD_NAME = "worker-thread-pool";
    private static ThreadPoolExecutor executor;
    private static volatile int threadNum = 0;

    /**
     * 初始化任务线程池
     *
     * @param threadCount 线程数量
     */
    public static synchronized void init(int threadCount) {
        if (executor != null) {
            return;
        }
        executor = new ThreadPoolExecutor(threadCount, threadCount, 0, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(MAX_WORK_COUNT), runnable -> {
            Thread thread = new Thread(runnable);
            synchronized (WorkerThreadPool.class) {
                thread.setName(THREAD_NAME + "-" + threadNum++);
            }
            thread.setDaemon(true);
            return thread;
        }, new ThreadPoolExecutor.CallerRunsPolicy() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                super.rejectedExecution(r, e);
            }
        });
    }

    /**
     * 加入任务
     *
     * @param runnable 待执行任务
     */
    public static void execute(Runnable runnable) {
        executor.execute(runnable);
    }

    /**
     * 获取线程池
     *
     * @return 线程池
     */
    public static ThreadPoolExecutor getExecutor() {
        return executor;
    }
}
