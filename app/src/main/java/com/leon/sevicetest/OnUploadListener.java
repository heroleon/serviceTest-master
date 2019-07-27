package com.leon.sevicetest;

public interface OnUploadListener {

    void onAllSuccess();

    void onAllFailed();

    void onThreadProgressChange(int position, int percent);

    void onThreadFinish(int position);

    void onThreadInterrupted(int position);
}
