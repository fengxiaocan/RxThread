package com.evil.rxlib;

/**
 * @author noah
 * @email fengxiaocan@gmail.com
 * @create 26/6/18
 * @desc 发射器
 */
public interface RxSubscriber<T> {
    void onSubscribe(RxEmitter<T> emitter);
}
