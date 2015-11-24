package com.example.tom.testtcp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MySocket.MySocketCallbacks {
    private static final String TAG = "TcpTest";
    private static final String IP = "222.164.128.37";
    private static final DateFormat DF = new SimpleDateFormat("HH:mm:ss");

    private static final int PORT1 = 7405;
    private static final int PORT2 = 443;
    private Button btnStart;
    private MySocket socket1;
    private MySocket socket2;
    private ProgressBar progressBar1;
    private ProgressBar progressBar2;
    private EditText ipField;
    private EditText port1Field;
    private EditText port2Field;
    private TextView log;
    private int nrConnected;
    private int socket1Count;
    private int socket2Count;
    private int port1;
    private int port2;
    private String ip;
    private Date date;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnStart = (Button) findViewById(R.id.MainActivity_btn_start);
        btnStart.setOnClickListener(this);
        progressBar1 = (ProgressBar) findViewById(R.id.MainActivity_pb_1);
        progressBar2 = (ProgressBar) findViewById(R.id.MainActivity_pb_2);
        ipField = (EditText) findViewById(R.id.MainActivity_et_ip);
        port1Field = (EditText) findViewById(R.id.MainActivity_et_port1);
        port2Field = (EditText) findViewById(R.id.MainActivity_et_port2);
        log = (TextView) findViewById(R.id.MainActivity_tv_log);

        //default values
        ipField.setText(IP);
        port1Field.setText(Integer.toString(PORT1));
        port2Field.setText(Integer.toString(PORT2));
        progressBar1.setMax(Constants.TEST_MSG_COUNT);
        progressBar2.setMax(Constants.TEST_MSG_COUNT);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");


    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (socket1 != null) {
            socket1.close();
        }
        if (socket2 != null) {
            socket2.close();
        }
        super.onDestroy();
    }

    private void write() {
        appendLog("Start writing messages...");
        for (int i = 0; i < Constants.TEST_MSG_COUNT; ++i) {
            Date date = new Date();
            String msg = "Hello " + i + " -- " + date;
            socket1.write(msg);
            socket2.write(msg);
        }
        appendLog("Writing messages done");
    }

    private void appendLog(String msg) {
        log.append(DF.format(new Date()) + ": " + msg + "\n");
    }

    private void start() {
        log.setText(""); //clear log
        appendLog("Sockets connecting...");
        btnStart.setEnabled(false);

        nrConnected = 0;
        socket1Count = 0;
        socket2Count = 0;
        date = new Date();
        port1 = Integer.parseInt(port1Field.getText().toString());
        port2 = Integer.parseInt(port2Field.getText().toString());
        ip = ipField.getText().toString();
        progressBar1.setProgress(0);
        progressBar1.setProgress(0);

        socket1 = new MySocket(ip, port1, this);
        socket1.start();
        socket2 = new MySocket(ip, port2, this);
        socket2.start();
    }

    @Override
    public void onClick(View v) {
        start();
    }


    @Override
    public void onConnected(final int port) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                appendLog("Socket " + port + " connected");
                nrConnected++;
                if (nrConnected == 2) {
                    write();
                }
            }
        });

    }

    @Override
    public void onError(final String msg, Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                appendLog("ERROR, check logcat: " + msg);
                btnStart.setEnabled(true);
            }
        });
    }

    @Override
    public void onMessageArrived(final int port, String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (port == port1) {
                    socket1Count++;
                    progressBar1.setProgress(socket1Count);
                    if (socket1Count == Constants.TEST_MSG_COUNT) {
                        long time = (new Date().getTime() - date.getTime()) / 1000;
                        appendLog("Socket " + port + " done: " + time + " sec");
                    }
                } else if (port == port2) {
                    socket2Count++;
                    progressBar2.setProgress(socket2Count);
                    if (socket2Count == Constants.TEST_MSG_COUNT) {
                        long time = (new Date().getTime() - date.getTime()) / 1000;
                        appendLog("Socket " + port + " done: " + time + " sec");
                    }
                } else {
                    MyLog.e(TAG, "Unknown port: " + port);
                }
                if (socket2Count == Constants.TEST_MSG_COUNT && socket1Count == Constants.TEST_MSG_COUNT) {
                    appendLog("Test Done!");
                    btnStart.setEnabled(true);
                    socket1.close();
                    socket1 = null;
                    socket2.close();
                    socket2 = null;
                }
            }
        });


    }
}
