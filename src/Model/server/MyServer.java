package model.server;

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

public class MyServer {
    private ServerSocket theServer;
    protected final int port;
    protected final ClientHandler ch;
    protected volatile boolean stop;

    public MyServer(int port, ClientHandler ch) {
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
            theServer = new ServerSocket(port, 0, InetAddress.getByName("0.0.0.0"));
            theServer.setSoTimeout(1000); // 1sec
            while (!stop) {
                try {
                    Socket aClient = theServer.accept(); // blocking call
                    System.out.println("\n** client connected **\n");
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
