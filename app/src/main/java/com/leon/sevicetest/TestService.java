package com.leon.sevicetest;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by leon
 * Date: 2019/7/26
 * Time: 11:53
 * Desc:
 */
public class TestService extends Service {
    int num = 0;
    private boolean isRun = true;
    private int downloadedNum = 0;
    private DownLoadBinder downLoadBinder = new DownLoadBinder();
    private Callback mCallBack;
    //private ExecutorService mExecutor;

   // private List<Future> futureList;
   // private List<UploadTask> uploadTasks;
    private ConcurrentHashMap<Integer, UploadTask> downloadMap = new ConcurrentHashMap<>();
    private UploadUtil mUploadUtil;
    private volatile boolean isRunning=false;//判断线程池是否运行 标志位
    private ArrayList<UploadBean> list;

    public int getIndex() {
        return num;
    }

    public void setIndex(int index) {
        num = index;
    }

    @Override
    public void onCreate() {
        super.onCreate();
       // mExecutor = Executors.newFixedThreadPool(2);
        list = new ArrayList<>();
        list.add(new UploadBean(0, (byte) 0));
        list.add(new UploadBean(0, (byte) 0));
        list.add(new UploadBean(0, (byte) 0));
        list.add(new UploadBean(0, (byte) 0));
        list.add(new UploadBean(0, (byte) 0));
        list.add(new UploadBean(0, (byte) 0));
        list.add(new UploadBean(0, (byte) 0));
        list.add(new UploadBean(0, (byte) 0));
        list.add(new UploadBean(0, (byte) 0));
        list.add(new UploadBean(0, (byte) 0));
      /*  futureList = new ArrayList<>();
        uploadTasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final UploadTask uploadTask = new UploadTask(i*200);
            uploadTask.setIndex(i);
            final int finalI = i;
            uploadTask.setOnThreadCallback(new UploadTask.OnThreadCallback() {
                @Override
                public void update(int num) {
                    if (mCallBack != null) {
                        mCallBack.getNum(finalI, num);
                    }
                }

                @Override
                public synchronized void success(int position) {
                    downloadedNum++;
                    for (Map.Entry<Integer,UploadTask> entry:downloadMap.entrySet()
                            ) {
                        Log.i("downloadMap", String.valueOf(entry.getKey())+"==="+position+">>>>>>"+downloadedNum);
                    }
                    Log.i("POSITION", String.valueOf(position));
                    downloadMap.remove(position);
                    if (downloadedNum < uploadTasks.size()) {
                        start();
                    } else {
                        downloadMap.clear();
                        downloadedNum = 0;
                        for (int j = 0; j < uploadTasks.size(); j++) {
                            uploadTasks.get(j).restart();
                            uploadTasks.get(j).setFinish();
                        }

                    }

                }
            });
            uploadTasks.add(uploadTask);
        }*/
    }
    private void startUpload(ArrayList<UploadBean> list){
        isRunning=true;

        mUploadUtil.submitAll(getApplicationContext(), list);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return downLoadBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("TESTSERVICE", "onStartCommand");
        num = intent.getIntExtra("index", 0);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.e("TESTSERVICE", "onDestroy");
        super.onDestroy();
    }

    /**
     * 内部类继承Binder
     *
     * @author lenovo
     */
    public class DownLoadBinder extends Binder {
        /**
         * 声明方法返回值是MyService本身
         *
         * @return
         */
        public TestService getService() {
            return TestService.this;
        }
    }

    public void setCallback(Callback callback) {
        mCallBack = callback;
    }

    /**
     * 回调接口
     *
     * @author lenovo
     */
    public interface Callback {
        /**
         * 得到实时更新的数据
         *
         * @return
         */
        void getNum(int position, int num);
    }

    public void start() {
       /* for (int i = 0; i < uploadTasks.size(); i++) {
            if (!uploadTasks.get(i).isFinish()) {
                if (downloadMap.size() < 2) {
                    downloadMap.put(i, uploadTasks.get(i));
                    futureList.add(mExecutor.submit(uploadTasks.get(i)));
                } else {
                    break;
                }
            }
        }
        for (Map.Entry<Integer,UploadTask> entry:downloadMap.entrySet()
             ) {
            Log.i("SIZE", String.valueOf(entry.getKey()));
        }*/
        mUploadUtil=new UploadUtil();
        mUploadUtil.setOnUploadListener(new OnUploadListener() {
            @Override
            public void onAllSuccess() {
                isRunning=false;
            }

            @Override
            public void onAllFailed() {
                isRunning=false;
            }

            @Override
            public void onThreadProgressChange(int position, int percent) {
                Log.v("TESTSERVICE", "进度："  + percent + ",正在执行第" + position + "个任务");
                list.get(position).setStatus((byte) 1);
                list.get(position).setUploadProgress(percent);
                if(mCallBack!=null){
                    mCallBack.getNum(position, percent);
                }
            }

            @Override
            public void onThreadFinish(int position) {
                list.get(position).setStatus((byte) 2);
            }

            @Override
            public void onThreadInterrupted(int position) {
                list.get(position).setStatus((byte) 3);
            }
        });
        if(!isRunning){
            startUpload(list);//开始上传
        }else{
            mUploadUtil.shutDownNow();//中断所有线程的执行
        }
    }

    public void restart() {
       /* for (int i = 0; i < downloadMap.size(); i++) {
            UploadTask currentTask = downloadMap.get(i);
            if (currentTask != null && !currentTask.isFinish()) {
                currentTask.restart();
                mExecutor.submit(downloadMap.get(i));
            }
        }*/

    }

    public void pause() {
      /*  for (int i = 0; i < downloadMap.size(); i++) {
            UploadTask currentTask = downloadMap.get(i);
            if (currentTask != null && !currentTask.isFinish()) {
                currentTask.cancle();
            }
        }*/
    }
}
