package com.evil.rxlib;

class TRunnable<T> implements Runnable {
    private T mData;

    public TRunnable(T data) {
        mData = data;
    }

    public T getData() {
        return mData;
    }

    public void setData(T data) {
        mData = data;
    }

    @Override
    public void run() {

    }
}
