package com.evil.rxlib;

/**
 * @author noah
 * @email fengxiaocan@gmail.com
 * @create 26/6/18
 * @desc 接收发射器发射的接收器
 */
public interface RxAcceptor<T> {
    void onStart();

    void onNext(T t);

    void onError(Throwable t);

    void onComplete();
}
