package com.example.tom.testtcp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class SocketReader implements Runnable {
    private static final String TAG = "TcpTestSR";
    private final Socket socket;
    private final int port;
    private boolean running = true;
    private int counter;
    private final MySocket.MySocketCallbacks cb;

    public SocketReader(int port, Socket socket, MySocket.MySocketCallbacks cb) {
        this.socket = socket;
        this.port = port;
        counter = 0;
        this.cb = cb;
    }

    @Override
    public void run() {
        try {
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            do {
                String line = inFromServer.readLine();
                if (line == null) {
                    MyLog.d(TAG, "Socket " + port + " got NULL");
                    break;
                }
                counter++;
                MyLog.d(TAG, "(" + counter + ") Read from " + port + ": " + line);
                cb.onMessageArrived(port, line);

                if (counter % Constants.TEST_MSG_COUNT == 0) {
                    MyLog.d(TAG, "------------------------------------------------------");
                    MyLog.d(TAG, "----- got " + counter + " messages on port " + port + " ---");
                    MyLog.d(TAG, "------------------------------------------------------");
                }
            } while (running);
        } catch (Exception e) {
            MyLog.e(TAG, "Error reading socket " + port, e);
        }
        MyLog.d(TAG, "Socket " + port + " closed");
    }

    public void stop() {
        running = false;
    }
}