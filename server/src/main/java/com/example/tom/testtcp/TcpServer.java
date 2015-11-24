package com.example.tom.testtcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class TCPServer {

    private boolean running = true;

    public static void main(String argv[]) throws Exception {
        TCPServer server = new TCPServer();
        List<Integer> ports = new ArrayList<>();
        for (String p : argv) {
            ports.add(Integer.parseInt(p));
        }
        if (ports.isEmpty()) {
            System.err.println("Please provide list of ports as argument");
        }
        server.run(ports);
    }

    public void run(List<Integer> ports) throws Exception {
        List<MyServerSocket> sockets = new ArrayList<>();
        for (Integer port : ports) {
            MyServerSocket socket = new MyServerSocket(port);
            socket.start();
        }
        System.out.println("Server started");
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        while (running) {
            String line = r.readLine();
            if (line.equalsIgnoreCase("quit")) {
                System.out.println("Quitting...");
                running = false;
            }
        }

        for (MyServerSocket socket : sockets) {
            socket.join();
        }

    }

    private class MyServerSocket extends Thread {
        private final int port;

        public MyServerSocket(int port) {
            this.port = port;
        }

        public void run() {
            String clientSentence;
            String capitalizedSentence;
            try {
                while (running) {
                    System.out.println("Creating socket on port " + port);
                    try (ServerSocket welcomeSocket = new ServerSocket(port);
                         Socket connectionSocket = welcomeSocket.accept();
                         BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                         //DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connectionSocket.getOutputStream()));
                    ) {


                        while (running) {
                            clientSentence = inFromClient.readLine();
                            if (clientSentence == null) {
                                System.out.println("Socket on port " + port + " closed");
                                break;
                            }
                            System.out.println("Received (" + port + "): " + clientSentence);
                            capitalizedSentence = clientSentence.toUpperCase();
                            writer.write(capitalizedSentence);
                            writer.newLine();
                            writer.flush();
                            //outToClient.writeBytes(capitalizedSentence);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Exception on socket " + port);
                e.printStackTrace();
            }
        }
    }
}
