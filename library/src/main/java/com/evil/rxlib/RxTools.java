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
 * 工具类
 */
final class RxTools {

    static boolean isAndroid;// Flag: is on android platform

    static void resetThread(Thread thread, final String name, final RxSubscribe callback) {
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if (callback != null) {
                    callback.onError(name, e);
                }
            }
        });
        thread.setName(name);
    }

    static boolean isEmpty(CharSequence data) {
        return data == null || data.length() == 0;
    }

    static {
        try {
            Class.forName("android.os.Build");
            isAndroid = true;
        } catch (Exception e) {
            isAndroid = false;
        }
    }
}