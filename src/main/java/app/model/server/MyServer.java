package app.model.server;

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

public class MyServer {
    public ServerSocket theServer;
    private final int port;
    private final ClientHandler ch;
    private volatile boolean stop;
    private List<Socket> clients;
    private static final int THREAD_POOL_SIZE = 3;

    public MyServer(int port, ClientHandler ch) {
        this.port = port;
        this.ch = ch;
        this.clients = new ArrayList<>();
    }

    public void start() {
        this.stop = false;
        new Thread(() -> {
            runServer();
        }).start();
    }

    private void runServer() {
        try {
            theServer = new ServerSocket(this.port);
            theServer.setSoTimeout(1000); // 1sec

            ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

            while (!stop) {
                try {
                    Socket aClient = theServer.accept(); // blocking call
                    this.clients.add(aClient);
                    executorService.execute(() -> {
                        try {
                            ClientHandler clientHandler = this.ch.getClass().getDeclaredConstructor().newInstance();
                            clientHandler.handleClient(aClient.getInputStream(), aClient.getOutputStream());
                            clientHandler.close();
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
                } catch (SocketTimeoutException e) {
                }
            }
            executorService.shutdownNow();
            theServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendToAll(String message) {
        for (Socket s : clients) {
            try {
                PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                out.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        this.stop = true;
    }
}
