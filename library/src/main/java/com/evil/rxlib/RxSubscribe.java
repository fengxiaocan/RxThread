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

/**
 * A callback interface to notify user that the task's status.
 * RX线程回调订阅器
 */
public interface RxSubscribe {

    /**
     * This method will be invoked when thread has been occurs an error.
     * @param tag The running thread name
     * @param t The exception
     */
    void onError(String tag,Throwable t);

    /**
     * notify user to know that it completed.
     * @param tag The running thread name
     */
    void onCompleted(String tag);

    /**
     * notify user that task start running
     * @param tag The running thread name
     */
    void onStart(String tag);
}
