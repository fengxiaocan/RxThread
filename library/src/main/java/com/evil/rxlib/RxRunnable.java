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

/**
 * The type Rx runnable.
 *  rx线程封装器
 * @param <T> the type parameter
 */
final class RxRunnable<T> implements Runnable {
    private String name;
    private SubscribeDelegate<T> delegate;
    private Runnable runnable;
    private Callable<T> callable;

    /**
     * Instantiates a new Rx runnable.
     *
     * @param rxConfigs the rx configs
     */
    RxRunnable(RxConfigs rxConfigs) {
        this.name = rxConfigs.name;
        this.delegate = new SubscribeDelegate<T>(
                rxConfigs.callback,
                rxConfigs.mExecutor,
                rxConfigs.asyncCallback
        );
    }

    /**
     * Runnable rx runnable.
     *
     * @param runnable the runnable
     * @return the rx runnable
     */
    RxRunnable runnable(Runnable runnable) {
        this.runnable = runnable;
        return this;
    }

    /**
     * Callable rx runnable.
     *
     * @param callable the callable
     * @return the rx runnable
     */
    RxRunnable callable(Callable<T> callable) {
        this.callable = callable;
        return this;
    }

    @Override
    public void run() {
        Thread current = Thread.currentThread();
        RxTools.resetThread(current,name,delegate);
        delegate.onStart(name);

        // avoid NullPointException
        if (runnable != null) {
            runnable.run();
        } else if (callable != null) {
            try {
                T result = callable.call();
                delegate.onSuccess(result);
            } catch (Exception e) {
                delegate.onError(name,e);
            }
        }
        delegate.onCompleted(name);
    }
}
