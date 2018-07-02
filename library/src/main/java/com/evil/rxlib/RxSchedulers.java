package com.evil.rxlib;


import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * @author noah
 * @email fengxiaocan@gmail.com
 * @create 26/6/18
 * @desc 线程调度器
 */
public class RxSchedulers<T> {
    private ExecutorService pool;
    private RxConfigs mRxConfigs;
    private RxSubscriber<T> subscriber;
    private Executor mSchedulersExecutor;
    private boolean isSyncComplete;//是否需要异步处理该线程

    RxSchedulers(ExecutorService pool,RxConfigs rxScheduler) {
        this.pool = pool;
        mRxConfigs = rxScheduler;
    }

    /**
     * 接收在主线程中进行
     *
     * @return
     */
    public RxSchedulers<T> acceptOnMain() {
        mSchedulersExecutor = RxExecutor.main();
        return this;
    }

    /**
     * 接收在相同线程中进行
     *
     * @return
     */
    public RxSchedulers<T> acceptOnSync() {
        mSchedulersExecutor = RxExecutor.sync();
        return this;
    }

    RxSchedulers<T> setSubscriber(RxSubscriber<T> subscriber) {
        this.subscriber = subscriber;
        return this;
    }

    public void acceptor(RxAcceptor<T> rxAcceptor) {
        if (mSchedulersExecutor == null) {
            mSchedulersExecutor = MainExecutor.getInstance();
        }
        mRxConfigs.callback = new SchedulersRxSubscribe(mRxConfigs.callback,rxAcceptor);

        isSyncComplete = mRxConfigs.mExecutor != mSchedulersExecutor &&
                         mRxConfigs.mExecutor instanceof MainExecutor;

        Runnable mRunnable = new RxRunnable(mRxConfigs).runnable(new TRunnable<RxAcceptor<T>>(
                rxAcceptor)
        {
            @Override
            public void run() {
                subscriber.onSubscribe(new RxEmitter<T>() {
                    private RxCounter rxCounter = new RxCounter(getData(),
                                                                isSyncComplete,
                                                                mRxConfigs.mExecutor);

                    @Override
                    public void onNext(final T t) {
                        rxCounter.scheduler();
                        mSchedulersExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                if (getData() != null) {
                                    getData().onNext(t);
                                }
                                rxCounter.complete();
                            }
                        });
                    }
                });
                if (!isSyncComplete) {
                    mRxConfigs.mExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            getData().onComplete();
                        }
                    });
                }
            }
        });
        DelayTaskDispatcher.get().postDelay(mRxConfigs.delay,pool,mRunnable);
    }

    private class SchedulersRxSubscribe implements RxSubscribe {
        private RxSubscribe mRxSubscribe;
        private RxAcceptor<T> rxAcceptor;

        public SchedulersRxSubscribe(RxSubscribe rxSubscribe,RxAcceptor<T> rxAcceptor) {
            mRxSubscribe = rxSubscribe;
            this.rxAcceptor = rxAcceptor;
        }

        @Override
        public void onError(String tag,Throwable t) {
            if (rxAcceptor != null) {
                rxAcceptor.onError(t);
            }
            if (mRxSubscribe != null) {
                mRxSubscribe.onError(tag,t);
            }
        }

        @Override
        public void onCompleted(String tag) {
            if (mRxSubscribe != null) {
                mRxSubscribe.onCompleted(tag);
            }
        }

        @Override
        public void onStart(String tag) {
            if (rxAcceptor != null) {
                rxAcceptor.onStart();
            }
            if (mRxSubscribe != null) {
                mRxSubscribe.onStart(tag);
            }
        }
    }
}
