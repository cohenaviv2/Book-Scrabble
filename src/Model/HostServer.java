package model;

import java.io.*;
import java.net.*;
import java.util.*;

import model.logic.Player;
import model.server.ClientHandler;
import model.server.GameServer;

/*
 * The host server responsible for the communication between the guests and the host.
 * Only hosts can conntect and communicate with this server.
 * Server supports up to 3 guest players.
 * The host server is connected to the game server.
 * 
 * @authors: Aviv Cohen, Moshe Azachi, Matan Eliyahu
 * 
 */

public class HostServer extends GameServer {
    private ServerSocket theHostServer;
    private Socket gameServer;
    private Map<Socket, Player> clients;
    private PrintWriter gameServerOut;
    private Scanner gameServerIn;
    private boolean ready = false;

    public HostServer(int port, ClientHandler ch) {
        super(port, ch);
        this.clients = new HashMap<>();
        Connect(); // to the game server
    }


    private void Connect() {
        /*
         * Connects to the game server
         * communicates via input and output streams
         */

        try {
            this.gameServer = new Socket("localhost", 12345);
            this.gameServerOut = new PrintWriter(gameServer.getOutputStream());
            this.gameServerIn = new Scanner(gameServer.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            theHostServer.setSoTimeout(1000); // 1sec
            while (!ready) {
                try {
                    Socket aClient = theHostServer.accept(); // blocking call
                    // this.ch.handleClient(aClient.getInputStream(), aClient.getOutputStream());
                    // this.ch.close();
                    aClient.close();
                } catch (SocketTimeoutException e) {
                }
            }
            theHostServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        this.ready = true;
    }

}
