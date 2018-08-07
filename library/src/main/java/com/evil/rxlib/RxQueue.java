package com.evil.rxlib;


import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * @author noah
 * @email fengxiaocan@gmail.com
 * @create 26/6/18
 * @desc 线程队列调度器
 */
public class RxQueue<T> {
    private ExecutorService pool;
    private RxConfigs mRxConfigs;
    private RxTask[] mRxTasks;
    private Executor mSchedulersExecutor;

    RxQueue(ExecutorService pool,RxConfigs rxScheduler) {
        this.pool = pool;
        mRxConfigs = rxScheduler;
    }

    /**
     * 接收在主线程中进行
     *
     * @return
     */
    public RxQueue<T> acceptOnMain() {
        mSchedulersExecutor = RxExecutor.main();
        return this;
    }

    /**
     * 接收在相同线程中进行
     *
     * @return
     */
    public RxQueue<T> acceptOnSync() {
        mSchedulersExecutor = RxExecutor.sync();
        return this;
    }

    RxQueue<T> setRxTasks(RxTask... tasks) {
        this.mRxTasks = tasks;
        return this;
    }

    public void launch() {
        if (mRxTasks == null || mRxTasks.length == 0) {
            return;
        }
        if (mSchedulersExecutor == null) {
            mSchedulersExecutor = MainExecutor.getInstance();
        }
        Runnable mRunnable = new RxRunnable(mRxConfigs).runnable(new TRunnable<RxTask[]>(mRxTasks) {
            @Override
            public void run() {
                RxTask[] tasks = getData();
                for (final RxTask rxTask : tasks) {
                    try {
                        rxTask.onRun();
                        mSchedulersExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    rxTask.onComplete();
                                } catch (Throwable throwable) {
                                    rxTask.onError(throwable);
                                }
                            }
                        });
                    } catch (Throwable throwable) {
                        rxTask.onError(throwable);
                    }
                }
            }
        });
        DelayTaskDispatcher.get().postDelay(mRxConfigs.delay,pool,mRunnable);
    }
}
