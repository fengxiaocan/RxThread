package com.evil.rxlib;

public interface RxTask {
    void onRun() throws Throwable;

    void onComplete() throws Throwable;

    void onError(Throwable throwable);
}
