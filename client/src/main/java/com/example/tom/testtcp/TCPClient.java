package com.example.tom.testtcp;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TCPClient {

    private static final String IP = "222.164.128.37";

    private static final int port1 = 7405;
    private static final int port2 = 443;
    private static final String TAG = "TCPClient";

    private static MySocket socket1;
    private static MySocket socket2;
    private static int countSocket1 = 0;
    private static int countSocket2 = 0;

    public static void main(String[] args) throws InterruptedException {


        MyLog.setLogger(new LogImpl());
        ExecutorService e = Executors.newCachedThreadPool();
        MySocket.setExecutor(e);


        MyLog.d(TAG, "Starting...");

        socket1 = new MySocket(IP, port1, cb);
        socket1.start();
        socket2 = new MySocket(IP, port2, cb);
        socket2.start();

        MyLog.d(TAG, "Waiting 5 sec to connect...");
        Thread.sleep(5000);

        MyLog.d(TAG, "Start writing!");
        for (int i = 0; i < Constants.TEST_MSG_COUNT; ++i) {
            Date date = new Date();
            String msg = "Hello " + i + " -- " + date;
            socket1.write(msg);
            socket2.write(msg);
        }
        Thread.sleep(1000);
        e.shutdown();
        e.awaitTermination(1, TimeUnit.HOURS);
    }

    private static class LogImpl implements MyLogIntf {
        private final DateFormat df = new SimpleDateFormat("HH:mm:ss");

        @Override
        public void d(String tag, String msg) {
            log("DEBUG", tag, msg, null);
        }

        @Override
        public void e(String tag, String s, Exception e) {
            log("ERROR", tag, s, e);
        }

        @Override
        public void d(String tag, String s, IOException e) {
            log("DEBUG", tag, s, e);

        }

        private void log(String level, String tag, String msg, Exception e) {
            String date = df.format(new Date());
            System.out.println(date + " [" + level + "][" + tag + "]" + msg);
            if (e != null) {
                e.printStackTrace();
            }
        }
    }

    private static final MySocket.MySocketCallbacks cb = new MySocket.MySocketCallbacks() {
        @Override
        public void onConnected(int port) {

        }

        @Override
        public void onError(String msg, Exception e) {

        }

        @Override
        public void onMessageArrived(int port, String msg) {

            if (port == port1) {
                countSocket1++;
                if (countSocket1 == Constants.TEST_MSG_COUNT) {
                    socket1.close();
                }
            } else if (port == port2) {
                countSocket2++;
                if (countSocket2 == Constants.TEST_MSG_COUNT) {
                    socket2.close();
                }
            }
        }
    };
}
