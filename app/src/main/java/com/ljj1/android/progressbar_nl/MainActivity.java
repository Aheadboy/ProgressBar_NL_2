package com.ljj1.android.progressbar_nl;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ITimeOutCallback {
    private HorizontalProgressBarWithNumber mProgressBar;
    private static final int MSG_PROGRESS_UPDATE = 0x110;

//    private Handler mHandler = new Handler() {
//        public void handleMessage(android.os.Message msg) {
//            int progress = mProgressBar.getProgress();
//            mProgressBar.setProgress(++progress);
//            if (progress >= 100) {
//                mHandler.removeMessages(MSG_PROGRESS_UPDATE);
//            }
//            mHandler.sendEmptyMessageDelayed(MSG_PROGRESS_UPDATE, 100);
//        };
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = (HorizontalProgressBarWithNumber) findViewById(R.id.id_progressbar01);
        mProgressBar.setOnTimeOutCallBack(MainActivity.this);////进度条初始化后，必须设置回调对象。否则nullEx

//        mHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);



        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.stop();
            }
        });
        findViewById(R.id.pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.pause();
            }
        });
        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.start();
            }
        });
    }


    @Override
    public void timeOut(int time) {
        Toast.makeText(MainActivity.this, "timeout in -- " + time, Toast.LENGTH_SHORT).show();
    }
}
