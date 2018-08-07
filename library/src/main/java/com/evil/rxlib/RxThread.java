/*
 * Copyright (C) 2017 Haoge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evil.rxlib;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 一个简化RxJava的模仿器,简单快捷省代码,只适合需要异步调用线程处理数据的调度,复杂的请使用rxJava或rxJava
 */
public final class RxThread implements Executor {
    private ExecutorService pool;
    // ==== There are default configs
    private String defName;// default thread name
    private RxSubscribe defCallback;// default thread callback
    private Executor defDeliver;// default thread mExecutor

    // to make sure no conflict for multi thread config.
    private ThreadLocal<RxConfigs> local;

    private RxThread(
            int type,
            int size,
            int priority,
            String name,
            RxSubscribe callback,
            Executor deliver,
            ExecutorService pool
    )
    {
        if (pool == null) {
            pool = createPool(type,size,priority);
        }
        this.pool = pool;
        this.defName = name;
        this.defCallback = callback;
        this.defDeliver = deliver;
        this.local = new ThreadLocal<>();
    }

    public static Proxy newThread() {
        return Proxy.newThread();
    }

    public static Proxy io() {
        return Proxy.io();
    }

    public static Proxy scheduled() {
        return Proxy.scheduled();
    }

    public static Proxy single() {
        return Proxy.single();
    }

    /**
     * 设置线程标记
     *
     * @return
     */
    public RxThread tag(String name) {
        getLocalConfigs().name = name;
        return this;
    }

    /**
     * 订阅线程回调
     *
     * @param callback
     * @return
     */
    public RxThread observe(RxSubscribe callback) {
        getLocalConfigs().callback = callback;
        return this;
    }

    /**
     * 延迟
     *
     * @param time
     * @param unit
     * @return
     */
    public RxThread delay(long time,TimeUnit unit) {
        long delay = unit.toMillis(time);
        getLocalConfigs().delay = Math.max(0,delay);
        return this;
    }

    /**
     * 结果回调所在的线程
     *
     * @return
     */
    public RxThread observeOnSync() {
        getLocalConfigs().mExecutor = RxExecutor.main();
        return this;
    }

    /**
     * 结果回调在主线程中
     *
     * @return
     */
    public RxThread observeOnMain() {
        getLocalConfigs().mExecutor = RxExecutor.main();
        return this;
    }

    /**
     * 直接异步线程
     *
     * @param runnable
     */
    @Override
    public void execute(Runnable runnable) {
        RxConfigs rxConfigs = getLocalConfigs();
        runnable = new RxRunnable<>(rxConfigs).runnable(runnable);
        DelayTaskDispatcher.get().postDelay(rxConfigs.delay,pool,runnable);
        resetLocalConfigs();
    }

    /**
     * 开启异步线程
     *
     * @param callable
     * @param <T>
     * @return
     */
    public <T> RxAsync<T> async(Callable<T> callable) {
        RxConfigs rxConfigs = getLocalConfigs();
        return new RxAsync<T>(pool,rxConfigs).callable(callable);
    }

    /**
     * 开启连续发射器,跟rxJava类型
     *
     * @param subscriber
     * @param <T>
     * @return
     */
    public <T> RxSchedulers<T> subscriber(RxSubscriber<T> subscriber) {
        return new RxSchedulers<T>(pool,getLocalConfigs()).setSubscriber(subscriber);
    }

    /**
     * 开启连续任务系列
     *
     * @return
     */
    public RxQueue subscriber(RxTask... tasks) {
        return new RxQueue(pool,getLocalConfigs()).setRxTasks(tasks);
    }

    public <T> Future<T> submit(Callable<T> callable) {
        Future<T> result;
        callable = new CallableWrapper<>(getLocalConfigs(),callable);
        result = pool.submit(callable);
        resetLocalConfigs();
        return result;
    }

    private ExecutorService createPool(int type,int size,int priority) {
        switch (type) {
            case Proxy.TYPE_CACHEABLE:
                return Executors.newCachedThreadPool(new DefaultFactory(priority));
            case Proxy.TYPE_FIXED:
                return Executors.newFixedThreadPool(size,new DefaultFactory(priority));
            case Proxy.TYPE_SCHEDULED:
                return Executors.newScheduledThreadPool(size,new DefaultFactory(priority));
            case Proxy.TYPE_SINGLE:
            default:
                return Executors.newSingleThreadExecutor(new DefaultFactory(priority));
        }
    }

    private synchronized void resetLocalConfigs() {
        local.set(null);
    }

    private synchronized RxConfigs getLocalConfigs() {
        RxConfigs rxConfigs = local.get();
        if (rxConfigs == null) {
            rxConfigs = new RxConfigs();
            rxConfigs.name = defName;
            rxConfigs.callback = defCallback;
            rxConfigs.mExecutor = defDeliver;
            local.set(rxConfigs);
        }
        return rxConfigs;
    }

    private static class DefaultFactory implements ThreadFactory {

        private int priority;

        DefaultFactory(int priority) {
            this.priority = priority;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setPriority(priority);
            return thread;
        }
    }

    public static class Proxy {
        final static int TYPE_CACHEABLE = 0;
        final static int TYPE_FIXED = 1;
        final static int TYPE_SINGLE = 2;
        final static int TYPE_SCHEDULED = 3;

        protected int type;
        protected int size;
        protected int priority = Thread.NORM_PRIORITY;
        protected String name;
        protected RxSubscribe callback;
        protected Executor deliver;
        protected ExecutorService pool;

        private Proxy(int size,int type,ExecutorService pool) {
            this.size = Math.max(1,size);
            this.type = type;
            this.pool = pool;
        }

        public static Proxy create(ExecutorService pool) {
            return new Proxy(1,TYPE_SINGLE,pool);
        }


        public static Proxy newThread() {
            return new Proxy(0,TYPE_CACHEABLE,null);
        }


        public static Proxy io(int size) {
            return new Proxy(size,TYPE_FIXED,null);
        }


        public static Proxy io() {
            return new Proxy(3,TYPE_FIXED,null);
        }


        public static Proxy scheduled(int size) {
            return new Proxy(size,TYPE_SCHEDULED,null);
        }


        public static Proxy scheduled() {
            return new Proxy(3,TYPE_SCHEDULED,null);
        }


        public static Proxy single() {
            return new Proxy(1,TYPE_SINGLE,null);
        }

        /**
         * 设置线程标记
         *
         * @return
         */
        public Proxy tag(String name) {
            if (!RxTools.isEmpty(name)) {
                this.name = name;
            }
            return this;
        }

        /**
         * 设置线程优先级
         *
         * @return
         */
        public Proxy priority(int priority) {
            this.priority = priority;
            return this;
        }

        /**
         * 线程调度回调
         *
         * @param callback
         * @return
         */
        public Proxy observe(RxSubscribe callback) {
            this.callback = callback;
            return this;
        }


        /**
         * 结果回调在主线程中
         *
         * @return
         */
        public Proxy observeOnMain() {
            this.deliver = RxExecutor.main();
            return this;
        }

        /**
         * 结果回调在相同线程中
         *
         * @return
         */
        public Proxy observeOnSync() {
            this.deliver = RxExecutor.sync();
            return this;
        }

        /**
         * 进入下一阶段
         *
         * @return
         */
        public RxThread open() {
            priority = Math.max(Thread.MIN_PRIORITY,priority);
            priority = Math.min(Thread.MAX_PRIORITY,priority);

            size = Math.max(1,size);
            if (RxTools.isEmpty(name)) {
                // set default thread name
                switch (type) {
                    case TYPE_CACHEABLE:
                        name = "CACHEABLE";
                        break;
                    case TYPE_FIXED:
                        name = "FIXED";
                        break;
                    case TYPE_SINGLE:
                        name = "SINGLE";
                        break;
                    default:
                        name = "RxThread";
                }
            }

            if (deliver == null) {
                if (RxTools.isAndroid) {
                    deliver = MainExecutor.getInstance();
                } else {
                    deliver = SyncExecutor.getInstance();
                }
            }

            return new RxThread(type,size,priority,name,callback,deliver,pool);
        }
    }
}
