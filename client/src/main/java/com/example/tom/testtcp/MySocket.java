package com.example.tom.testtcp;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.Executor;

public class MySocket implements Runnable, Closeable {
    private static final String TAG = "TcpTestS";
    private Socket clientSocket1;
    private SocketReader reader1;
    private BufferedWriter writer1;
    private final int port;
    private final String ip;
    private static Executor myExecutor = null;
    private final MySocketCallbacks cb;

    public MySocket(String ip, int port, MySocketCallbacks cb) {
        this.port = port;
        this.ip = ip;
        if (myExecutor == null) {
            throw new IllegalStateException("call setExecutor first");
        }
        this.cb = cb;
    }

    public static void setExecutor(Executor executor) {
        myExecutor = executor;
    }


    @Override
    public void run() {
        try {
            MyLog.d(TAG, "Creating socket on port " + port);
            clientSocket1 = new Socket(ip, port);
            reader1 = new SocketReader(port, clientSocket1, cb);
            myExecutor.execute(reader1);
            writer1 = new BufferedWriter(new OutputStreamWriter(clientSocket1.getOutputStream()));
            //DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            MyLog.d(TAG, "Socket on port " + port + " created");
            cb.onConnected(port);

        } catch (Exception e) {
            MyLog.e(TAG, "Socket error", e);
            cb.onError("Socket " + port + " error", e);
        }
    }

    public void start() {
        myExecutor.execute(this);
    }

    @Override
    public void close() {
        try {
            writer1.close();
            reader1.stop();
            clientSocket1.close();
        } catch (Exception e) {
            //ignore
        }
    }

    public void write(String msg) {
        MyLog.d(TAG, "Writing (" + port + "): " + msg);
        try {
            writer1.write(msg);
            writer1.newLine();
            writer1.flush();
        } catch (IOException e) {
            MyLog.d(TAG, "Error writing", e);
        }
    }

    public interface MySocketCallbacks {

        void onConnected(int port);

        void onError(String msg, Exception e);

        void onMessageArrived(int port, String msg);
    }
}
