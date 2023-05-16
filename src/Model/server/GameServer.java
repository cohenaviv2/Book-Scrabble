package Model.Server;

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

public class GameServer {
    protected final int port;
    private final ClientHandler ch;
    private volatile boolean ready;

    public GameServer(int port, ClientHandler ch) {
        this.port = port;
        this.ch = ch;
    }

    public void start() {
        this.ready = false;
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
            while (!ready) {
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
        this.ready = true;
    }

}
