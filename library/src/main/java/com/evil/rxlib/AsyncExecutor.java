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

import android.os.Looper;

import java.util.concurrent.Executor;

/**
 * The deliver for <b>Android Platform</b> by default.
 * 异步回调运行器
 */
final class AsyncExecutor implements Executor {

    private static AsyncExecutor instance = new AsyncExecutor();

    static AsyncExecutor getInstance() {
        return instance;
    }

    @Override
    public void execute(final Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            //如果当前是主线程,使用子线程
            RxThread.Proxy.newThread().observeOnSync().open().execute(runnable);
            return;
        }
        //如果当前是子线程
        MainExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                runnable.run();
            }
        });
    }
}
