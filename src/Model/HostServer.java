package Model;

import java.io.*;
import java.net.*;
import java.util.*;

import Model.Server.ClientHandler;
import Model.Server.GameServer;

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
    private Player hostPlayer;
    private Socket gameServer;
    private Map<Socket, Player> clients;
    private PrintWriter gameServerOut;
    private Scanner gameServerIn;
    private boolean ready = false;

    public HostServer(Player host, int port, ClientHandler ch) {
        super(port, ch);
        this.clients = new HashMap<>();
        this.hostPlayer = host;
        Connect(); // to the game server
    }

    private void Connect() {
        /*
         * Connects to the game server
         * communicates via input and output streams
         */

        try {
            gameServer = new Socket("localhost", 12345);
            gameServerOut = new PrintWriter(gameServer.getOutputStream());
            gameServerIn = new Scanner(gameServer.getInputStream());
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
            ServerSocket theServer = new ServerSocket(this.port);
            theServer.setSoTimeout(1000); // 1sec
            while (!ready) {
                try {
                    Socket aClient = theServer.accept(); // blocking call
                    // this.ch.handleClient(aClient.getInputStream(), aClient.getOutputStream());
                    // this.ch.close();
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
