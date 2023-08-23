package app.model.server;

import java.io.*;
import java.net.*;

/*
 * A generic server that implements ClientHandler interface designed to handle the client.
 * the interface defines a communication method (handleClient) and close method for the streams.
 * The server handles clients One by One.
 * 
 * @author: Aviv Cohen
 * 
 */

public class MyOldServer {
    protected final int port;
    protected final ClientHandler ch;
    protected volatile boolean stop;

    public MyOldServer(int port, ClientHandler ch) {
        this.port = port;
        this.ch = ch;
    }

    public void start() {
        this.stop = false;
        new Thread(() -> {
            try {
                runServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void runServer() throws Exception {
        try {
            ServerSocket theServer = new ServerSocket(this.port);
            theServer.setSoTimeout(1000); // 1sec
            while (!stop) {
                try {
                    Socket aClient = theServer.accept(); // blocking call
                    System.out.println("New client connected: " + aClient.getInetAddress().getHostAddress());
                    this.ch.handleClient(aClient.getInputStream(), aClient.getOutputStream());
                    System.out.println("\n** end handle client **\n");
                    this.ch.close();
                    aClient.close();
                } catch (SocketTimeoutException e) {
                }
            }
            theServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        this.stop = true;
    }

}
