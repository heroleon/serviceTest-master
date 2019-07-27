package com.leon.sevicetest;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.Random;

/**
 * Created by leon
 * Date: 2019/7/27
 * Time: 16:47
 * Desc:
 */
public class UploadTask implements Runnable {
    private int num = 0;
    private volatile boolean isPause = false;
    private volatile boolean isFinish = false;
    private OnThreadCallback mListener;
    private int index;
    private int sleep;
    public UploadTask(int time){
        sleep = time;
    }
    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        Log.v("TESTSERVICE", "线程：" + threadName + ",正在执行第" + index + "个任务");
        while (num <= 100 && !isPause) {
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            num = num + 20;
            Log.v("TESTSERVICE", "线程：" + threadName + "---" + num + ",正在执行第" + index + "个任务");
            if (num == 100) {
                isPause = true;
                isFinish = true;
               mHandler.sendEmptyMessageDelayed(1, new Random().nextInt(1000 - 500 + 1) + 500);
               // mHandler.sendEmptyMessage(1);
            }
            mHandler.sendEmptyMessage(0);
        }
    }

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int code = msg.what;
            switch (code) {
                case 0:
                    mListener.update(num);
                    break;
                case 1:
                    mListener.success(index);
                    break;

            }
        }
    };

    public void setOnThreadCallback(OnThreadCallback callback) {
        mListener = callback;
    }

    public void cancle() {
        isPause = true;
    }

    public void restart() {
        isPause = false;
    }

    public void setIndex(int i) {
        index = i;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public void setFinish() {
        isFinish = false;
    }

    public interface OnThreadCallback {
        void update(int num);

        void success(int position);
    }
}
