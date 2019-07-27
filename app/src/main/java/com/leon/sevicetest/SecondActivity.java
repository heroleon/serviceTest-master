package com.leon.sevicetest;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon
 * Date: 2019/7/26
 * Time: 14:06
 * Desc:
 */
public class SecondActivity extends Activity implements TestService.Callback {
    private TestService myService;
    private ProgressBar progressBar1;
    private ProgressBar progressBar2;
    private ProgressBar progressBar3;
    private ProgressBar progressBar4;
    private ProgressBar progressBar5;
    private ProgressBar progressBar6;
    private ProgressBar progressBar7;
    private ProgressBar progressBar8;
    private ProgressBar progressBar9;
    private ProgressBar progressBar10;
    private List<ProgressBar> progressBars = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        progressBar1 = findViewById(R.id.progress_bar1);
        progressBar2 = findViewById(R.id.progress_bar2);
        progressBar3 = findViewById(R.id.progress_bar3);
        progressBar4 = findViewById(R.id.progress_bar4);
        progressBar5 = findViewById(R.id.progress_bar5);
        progressBar6 = findViewById(R.id.progress_bar6);
        progressBar7 = findViewById(R.id.progress_bar7);
        progressBar8 = findViewById(R.id.progress_bar8);
        progressBar9 = findViewById(R.id.progress_bar9);
        progressBar10 = findViewById(R.id.progress_bar10);

        progressBars.add(progressBar1);
        progressBars.add(progressBar2);
        progressBars.add(progressBar3);
        progressBars.add(progressBar4);
        progressBars.add(progressBar5);
        progressBars.add(progressBar6);
        progressBars.add(progressBar7);
        progressBars.add(progressBar8);
        progressBars.add(progressBar9);
        progressBars.add(progressBar10);
        Intent intent = new Intent(this, TestService.class);
        startService(intent);
        bindService(intent, connection, 0);
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("TESTSERVICE", "onServiceDisconnected");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("TESTSERVICE", "onServiceConnected");
            myService = ((TestService.DownLoadBinder) service).getService();
            myService.setCallback(SecondActivity.this);
            myService.start();
            //int serviceIndex = myService.getIndex();
         /*   //是否在下载
            if (serviceIndex != 0) {
                myService.start();
            } else {
                myService.setIndex(10);
                myService.start();
            }*/
        }
    };


    public void startService(View view) {
        Intent intent = new Intent(this, TestService.class);
        startService(intent);
    }

    public void stopService(View view) {
        if (myService != null) {
            myService.pause();
        }

    }

    public void bindService(View view) {
        Intent intent = new Intent(this, TestService.class);
        intent.putExtra("index", 0);
        bindService(intent, connection, 0);
    }

    public void unBindService(View view) {
        if (myService != null) {
            myService.restart();
        }
    }

    @Override
    public void getNum(final int position, final int num) {
        progressBars.get(position).setProgress(num);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myService.setCallback(null);
        myService = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("TESTSERVICE", "onPause");
    }



}
