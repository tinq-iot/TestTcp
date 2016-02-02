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
    private boolean connected = false;
    private int count;

    public MySocket(String ip, int port, MySocketCallbacks cb) {
        this.port = port;
        this.ip = ip;
        if (myExecutor == null) {
            throw new IllegalStateException("call setExecutor first");
        }
        this.cb = cb;
        count = 0;
    }

    public static void setExecutor(Executor executor) {
        myExecutor = executor;
    }

    public int getPort() {
        return port;
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
            connected = true;
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
        } catch (Exception e) {
            //ignore
        }
        try {
            reader1.stop();
        } catch (Exception e) {
            //ignore
        }
        try {
            clientSocket1.close();
        } catch (Exception e) {
            //ignore
        }
    }

    public void write(String msg) {
        if (!connected) {
            throw new IllegalStateException("Socket not yet connected");
        }
        count++;
        MyLog.d(TAG, "Writing (" + port + "): message " + count);
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
