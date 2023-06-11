package model.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/*
 * A generic server that implements ClientHandler interface designed to handle the client.
 * the interface defines a communication method (handleClient) and close method for the streams.
 * The server handles clients One by One.
 * 
 * @author: Aviv Cohen
 * 
 */

 public class MyServerParallel {
    protected final int port;
    protected final ClientHandler ch;
    protected volatile boolean stop;
    private static Map<Integer, Socket> clients = new HashMap<>();

    private static final int THREAD_POOL_SIZE = 3;

    public MyServerParallel(int port, ClientHandler ch) {
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

            ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

            while (!stop) {
                try {
                    Socket aClient = theServer.accept(); // blocking call
                    System.out.println("SERVER: NEW CLIENT CONNECTED: " + aClient.getInetAddress().getHostAddress());

                    executorService.execute(() -> {
                        try {
                            ClientHandler clientHandler = this.ch.getClass().getDeclaredConstructor().newInstance();
                            clientHandler.handleClient(aClient.getInputStream(), aClient.getOutputStream());
                            System.out.println("** SERVER: CHAT ENDED **");
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                aClient.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    System.out.println("** SERVER:  END ACCEPTING CLIENT -> NEXT CLIENT **");
                } catch (SocketTimeoutException e) {
                }
            }

            executorService.shutdown();
            this.ch.close();
            theServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        this.stop = true;
    }
}
