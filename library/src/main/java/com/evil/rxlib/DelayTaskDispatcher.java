package com.evil.rxlib;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * The class to launch the delay-task with a core Scheduled pool.
 * 延迟计划调度器
 */
final class DelayTaskDispatcher {
    private static DelayTaskDispatcher instance = new DelayTaskDispatcher();
    private ScheduledExecutorService dispatcher;

    private DelayTaskDispatcher() {
        dispatcher = Executors.newScheduledThreadPool(1,new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                thread.setName("Delay-Task-Dispatcher");
                thread.setPriority(Thread.MAX_PRIORITY);
                return thread;
            }
        });
    }

    static DelayTaskDispatcher get() {
        return instance;
    }

    void postDelay(long delay,final ExecutorService pool,final Runnable task) {
        if (delay <= 0) {
            pool.execute(task);
            return;
        }

        ScheduledFuture<?> future = dispatcher.schedule(new Runnable() {
            @Override
            public void run() {
                pool.execute(task);
            }
        },delay,TimeUnit.MILLISECONDS);
    }

}
