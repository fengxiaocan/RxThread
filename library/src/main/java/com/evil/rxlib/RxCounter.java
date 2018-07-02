package com.evil.rxlib;

import java.util.concurrent.Executor;

/**
 * @author noah
 * @email fengxiaocan@gmail.com
 * @create 26/6/18
 * @desc 计算线程计划器,防止主线程以及异步线程之间成功的回调的结果提前调用
 */
class RxCounter<T> {
    long schedulerNumber = 0;//计划数目
    long completeNumber = 0;//完成数目
    RxAcceptor<T> rxAcceptor;
    private boolean isSyncComplete;
    private Executor mCompleteExecutor;

    public RxCounter(
            RxAcceptor<T> rxAcceptor,boolean isSyncComplete,Executor executor
    )
    {
        this.rxAcceptor = rxAcceptor;
        this.isSyncComplete = isSyncComplete;
        this.mCompleteExecutor = executor;
    }

    public synchronized void scheduler() {
        this.schedulerNumber++;
    }

    public synchronized boolean isComplete() {
        return completeNumber >= schedulerNumber;
    }

    public synchronized void complete() {
        this.completeNumber++;
        if (isSyncComplete) {
            //非相同的线程,
            if (isComplete()) {
                if (rxAcceptor != null) {
                    mCompleteExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            rxAcceptor.onComplete();
                        }
                    });
                }
            }
        }
    }
}
