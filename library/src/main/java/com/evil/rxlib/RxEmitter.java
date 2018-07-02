package com.evil.rxlib;

/**
 * @author noah
 * @email fengxiaocan@gmail.com
 * @create 26/6/18
 * @desc 发射器
 */
public interface RxEmitter<T> {
    void onNext(T t);
}
