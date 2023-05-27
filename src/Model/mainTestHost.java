package model;


import model.server.BookScrabbleHandler;
import model.server.MyServer;

public class mainTestHost {
    public static void main(String[] args) {

        //Create Game server:
        MyServer gs = new MyServer(5040, new BookScrabbleHandler());

        //Create Host model:
        HostModel hm = HostModel.getHM(); //creates host server on port 8040

        //Connect to the game server:
        hm.connectMe("Aviv", null, 5040);
    }
}
