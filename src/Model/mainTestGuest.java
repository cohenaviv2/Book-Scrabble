package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;

public class mainTestGuest {
    public static void main(String[] args) {

        // Create Guest model:
        GuestModel gs = new GuestModel();

        // Connect to the host server:
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        //PrintWriter out = new PrintWriter(System.out, true);
        String name = null;
        InetAddress ip = null;
        int port = 0;
        try {
            System.out.println("Enter your name,then press enter: ");
            name = in.readLine();
            System.out.println("Enter ip,then press enter: ");
            ip = InetAddress.getByName(in.readLine());
            System.out.println("Enter port,then press enter: ");
            port = Integer.parseInt(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }

        gs.connectMe(name ,ip, port);

    }

}
