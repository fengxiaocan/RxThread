package com.evil.rxlib;

import java.util.concurrent.Executor;

/**
 * @author noah
 * @email fengxiaocan@gmail.com
 * @create 26/6/18
 * @desc ...
 */
public class RxExecutor {
    public static Executor main() {
        return MainExecutor.getInstance();
    }

    public static Executor sync() {
        return SyncExecutor.getInstance();
    }
}
