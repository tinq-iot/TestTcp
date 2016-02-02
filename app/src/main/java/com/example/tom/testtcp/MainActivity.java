package com.example.tom.testtcp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
    private EditText packetSizeField;
    private EditText nrPacketsField;
    private TextView log;
    private int nrConnected;
    private int socket1Count;
    private int socket2Count;
    private int port1;
    private int port2;
    private String ip;
    private Date date;
    private int packetSize;
    private String msgBase;
    private int nrMessages;
    private WriteTask writeTask1;
    private WriteTask writeTask2;


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
        packetSizeField = (EditText) findViewById(R.id.MainActivity_et_packetSize);
        packetSizeField.setText(Integer.toString(20));
        nrPacketsField = (EditText) findViewById(R.id.MainActivity_et_nrPackets);
        nrPacketsField.setText(Integer.toString(Constants.TEST_MSG_COUNT));
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
        btnStart.setEnabled(true);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
        appendLog("Test stopped");
        if (writeTask1 != null) {
            writeTask1.cancel(true);
        }
        if (writeTask2 != null) {
            writeTask2.cancel(true);
        }
        if (socket1 != null) {
            socket1.close();
        }
        if (socket2 != null) {
            socket2.close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void write() {
        appendLogUi("Start writing " + nrMessages + " messages...");
        writeTask1 = new WriteTask(socket1);
        writeTask1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        writeTask2 = new WriteTask(socket2);
        writeTask2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void appendLog(String msg) {
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
        packetSize = Integer.parseInt(packetSizeField.getText().toString());
        if (packetSize < 15) {
            Toast.makeText(this, "Packet size must be >= 15", Toast.LENGTH_LONG).show();
            return;
        }
        StringBuilder msgBaseBuilder = new StringBuilder(packetSize - 15);
        for (int i = 0; i < (packetSize - 15); ++i) {
            msgBaseBuilder.append("x");
        }
        nrMessages = Integer.parseInt(nrPacketsField.getText().toString());
        msgBase = msgBaseBuilder.toString();
        ip = ipField.getText().toString();
        progressBar1.setMax(nrMessages);
        progressBar2.setMax(nrMessages);
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
        appendLogUi("Socket " + port + " connected");
        nrConnected++;
        if (nrConnected == 2) {
            write();
        }
    }

    private void appendLogUi(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                appendLog(msg);
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
                    if (socket1Count == nrMessages) {
                        long time = (new Date().getTime() - date.getTime()) / 1000;
                        appendLog("Socket " + port + " done: " + time + " sec");
                    }
                } else if (port == port2) {
                    socket2Count++;
                    progressBar2.setProgress(socket2Count);
                    if (socket2Count == nrMessages) {
                        long time = (new Date().getTime() - date.getTime()) / 1000;
                        appendLog("Socket " + port + " done: " + time + " sec");
                    }
                } else {
                    MyLog.e(TAG, "Unknown port: " + port);
                }
                if (socket2Count == nrMessages && socket1Count == nrMessages) {
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

    private final class WriteTask extends AsyncTask<Void, Void, Void> {
        private final MySocket socket;

        public WriteTask(MySocket socket) {
            this.socket = socket;
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (int i = 0; i < nrMessages; ++i) {
                if (isCancelled()) {
                    break;
                }
                String msg = String.format(Locale.ENGLISH, "%s %05d %s", msgBase, i, DF.format((new Date())));
                socket.write(msg);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            appendLog("Writing messages on port " + socket.getPort() + " done");
        }


    }
}
