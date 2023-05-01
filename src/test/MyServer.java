package test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/*
 * A generic server that implements ClientHandler interface designed to handle the client.
 * the interface defines a communication method (handleClient) and close method for the streams.
 * The server handles clients one by one.
 * 
 * @author: Aviv Cohen
 * 
 */

public class MyServer {
    int port;
    ClientHandler ch;
    boolean stop;

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
            ServerSocket theServer = new ServerSocket(this.port);
            theServer.setSoTimeout(1000); // 1sec
            while (!stop) {
                try {
                    Socket aClient = theServer.accept(); // blocking call
                    this.ch.handleClient(aClient.getInputStream(), aClient.getOutputStream());
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
