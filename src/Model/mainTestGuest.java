package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class mainTestGuest {
    public static void main(String[] args) {

        // Create Guest model:
        GuestModel gs = new GuestModel();

        // Connect to the host server:
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        //PrintWriter out = new PrintWriter(System.out, true);
        String name = null;
        String ip = null;
        int port = 0;
        try {
            System.out.println("Enter your name,then press enter: ");
            name = in.readLine();
            System.out.println("Enter ip,then press enter: ");
            ip = in.readLine();
            if (ip == null) {
                System.out.println("inValid ip");
            }
            System.out.println("Enter port,then press enter: ");
            port = Integer.parseInt(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }

        gs.connectMe(name ,ip, port);

    }

}
