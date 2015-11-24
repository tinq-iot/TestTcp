package com.example.tom.testtcp;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;


public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MyLog.setLogger(new LogImpl());

        MySocket.setExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class LogImpl implements MyLogIntf {

        @Override
        public void d(String tag, String msg) {
            Log.d(tag, msg);
        }

        @Override
        public void e(String tag, String s, Exception e) {
            Log.e(tag, s, e);
        }

        @Override
        public void d(String tag, String s, IOException e) {
            Log.d(tag, s, e);

        }
    }
}
