package com.evil.rxlib;

import java.util.concurrent.Executor;

/**
 * Store some configurations for sync task.
 * 线程配置
 */
final class RxConfigs {
    String name;// thread name
    RxSubscribe callback;// thread callback
    long delay;// delay time
    Executor mExecutor;// thread mExecutor
    AsyncCallback asyncCallback;
}
