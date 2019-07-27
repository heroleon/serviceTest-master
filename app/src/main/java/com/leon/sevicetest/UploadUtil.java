package com.leon.sevicetest;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Random;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UploadUtil {
    //线程进度回调
    private final static int THREAD_PROGRESS_CODE = 100;
    //线程完成
    private final static int THREAD_FINISH_CODE = 101;
    //线程被中断
    private final static int THREAD_INTERRUPT_CODE = 102;
    //所有线程完成
    private final static int THREAD_ALL_SUCCESS_CODE = 103;
    //所有线程执行失败
    private final static int THREAD_ALL_FAILED_CODE = 104;
    //handler传递进度值
    private final static String THREAD_PERCENT = "THREAD_PERCENT";
    //handler传递position值
    private final static String THREAD_POSITION = "THREAD_POSITION";
    //线程池核心数
    private int threadCore = 2;
    //线程池
    private ExecutorService executor;
    //成功数量
    int successCount = 0;
    //失败数量
    int failedCount = 0;

    private OnUploadListener uploadListener;
    private UploadHandler handler;

    public UploadUtil() {
        init();
    }

    public UploadUtil(int threadCore) {
        this.threadCore = threadCore;
        init();
    }

    public void setOnUploadListener(OnUploadListener uploadListener) {
        this.uploadListener = uploadListener;
    }

    public void init() {
        handler = new UploadHandler(this);
        successCount = 0;
        failedCount = 0;
    }

    public void shutDownNow() {
        //中断所有线程的执行
        if(executor!=null){
            executor.shutdownNow();
        }
    }

    public void submitAll(Context mContext, final ArrayList<UploadBean> list) {

        executor = Executors.newFixedThreadPool(threadCore);


        int i = 0;
        //遍历文件
        for (UploadBean uploadBean : list) {

            final Bundle bundle = new Bundle();
            bundle.putInt(THREAD_POSITION, i);
            i++;

            executor.submit(new UploadFile(new Random().nextInt(1000-500)+500, i, list, new OnThreadResultListener() {
                @Override
                public void onProgressChange(final int percent) {
                    bundle.putInt(THREAD_PERCENT, percent);
                    Message.obtain(handler, THREAD_PROGRESS_CODE, bundle).sendToTarget();
                }

                @Override
                public void onFinish() {
                    successCount++;
                    if (successCount == list.size()) {
                        successCount = 0;
                        failedCount = 0;
                        handler.sendEmptyMessage(THREAD_ALL_SUCCESS_CODE);
                    }
                    Message.obtain(handler, THREAD_FINISH_CODE, bundle).sendToTarget();
                }

                @Override
                public void onInterrupted() {
                    failedCount++;
                    if (successCount + failedCount == list.size()) {
                        successCount = 0;
                        failedCount = 0;
                        handler.sendEmptyMessage(THREAD_ALL_FAILED_CODE);
                    }
                    Message.obtain(handler, THREAD_INTERRUPT_CODE, bundle).sendToTarget();
                }
            }));
        }
        //关闭线程池
        executor.shutdown();
    }

    private static class UploadHandler extends Handler {//静态内部类+弱引用防止内存泄漏
        private WeakReference<UploadUtil> weakReference;

        private UploadHandler(UploadUtil object) {
            super(Looper.getMainLooper());//执行在UI线程
            weakReference = new WeakReference<>(object);
        }

        @Override
        public void handleMessage(Message msg) {
            UploadUtil uploadUtil = weakReference.get();
            if (uploadUtil != null) {
                Bundle data = (Bundle) msg.obj;
                int position;
                int percent;
                switch (msg.what) {
                    case THREAD_PROGRESS_CODE:
                        position = data.getInt(THREAD_POSITION);
                        percent = data.getInt(THREAD_PERCENT);
                        uploadUtil.uploadListener.onThreadProgressChange(position, percent);
                        break;
                    case THREAD_FINISH_CODE:
                        position = data.getInt(THREAD_POSITION);
                        uploadUtil.uploadListener.onThreadFinish(position);
                        break;
                    case THREAD_INTERRUPT_CODE:
                        position = data.getInt(THREAD_POSITION);
                        uploadUtil.uploadListener.onThreadInterrupted(position);
                        break;
                    case THREAD_ALL_SUCCESS_CODE:
                        uploadUtil.uploadListener.onAllSuccess();
                        break;
                    case THREAD_ALL_FAILED_CODE:
                        uploadUtil.uploadListener.onAllFailed();
                        break;
                }
            }
        }
    }
}







