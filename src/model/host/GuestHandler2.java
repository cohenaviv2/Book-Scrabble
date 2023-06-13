package model.host;

import java.io.*;
import java.net.*;
import java.util.*;
import model.game.GameManager;
import model.server.ClientHandler;

/*
 * The Guest handler used to communicate between the host and the guests
 * Communication is done using strings
 * 
 * HOST:
 * receives a string from the guest
 * starting with the guests ID,
 * then Model method to active,
 * and then the value (like query word)
 * All 3 parameters seperated by ","
 * 
 * e.g. - "0,connectMe,Moshe" , "0,getMyID,Moshe" , "259874,tryPlaceWord,Hello"
 * (ID is 0 for initialization)
 * 
 * GUEST:
 * gets a string from the host
 * starting with his ID,
 * then the Model methos that was activated
 * and then the returned value
 * All 3 parameters seperated by ","
 * 
 * e.g. - "0,connectMe,true" , "0,getMyID,256874" , "259874,tryPlaceWord,32"
 * 
 * @author: Aviv Cohen
 * 
 */

public class GuestHandler2 implements ClientHandler {

    private final GameManager gameManager;
    private int GUEST_ID;
    private String QUIT_GAME_ID;
    private String YOUR_TURN_ID;
    private String CONNECTION_ID;

    public GuestHandler2() {
        this.gameManager = GameManager.getGM();
    }

    @Override
    public void handleClient(InputStream inputStream, OutputStream outputStream) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                PrintWriter out = new PrintWriter(outputStream, true)) {

            String firstLine = in.readLine();

            if (connectGuest(firstLine)) { // The connection was successful - initial details, send ID and starts Chat

                out.println(CONNECTION_ID); // send ack and id to the guest
                // PRINT DEBUG
                System.out.println("GUEST HANDLER: guest " + GUEST_ID + " connected! starting Chat...\n");
                String guestMessage;

                // START CHAT
                while (!(guestMessage = in.readLine()).equals(QUIT_GAME_ID)) {
                    String[] params = guestMessage.split(",");
                    int id = Integer.parseInt(params[0]);
                    String modifier = params[1];
                    String value = params[2];

                    // Check if it's currect ID, if not drops the message
                    if (id == GUEST_ID) {

                        // Process guest's instructions
                        String returnValue = gameManager.processGuestInstruction(id, modifier, value);

                        // Send response to the guest
                        String response = GUEST_ID + "," + modifier + "," + returnValue;
                        out.println(response);

                        // Check if it's the guest's turn to play
                        if (isChanged()) {

                        }
                        if (isGuestTurn()) {

                            // Prompt the guest to make a move
                            out.println(YOUR_TURN_ID + "true");
                            String guestMove = in.readLine();
                            String moveParams[] = guestMove.split(",");
                            int moveId = Integer.parseInt(moveParams[0]);
                            String moveMod = moveParams[1];
                            String moveVal = moveParams[2];

                            if (moveId == GUEST_ID) {
                                // Process the guest's move
                                String moveReturnVal = gameManager.processGuestMove(moveId, moveMod, moveVal);

                                // Send the move response to the guest
                                String moveResponse = GUEST_ID + "," + moveMod + "," + moveReturnVal;
                                out.println(moveResponse);
                            }

                        } else {
                            // It's not the guest's turn, sends a message
                            // out.println(YOUR_TURN_ID + "false");
                        }
                    }

                }
                // Guest chose to quit game - chat ended
                gameManager.quitGameHandler(QUIT_GAME_ID);
                out.println(QUIT_GAME_ID);
                // PRINT DEBUG
                System.out.println("GUEST HANDLER: chat ended, " + GUEST_ID + " has quit the game\n");
            } else {
                // PRINT DEBUG
                System.out.println("GUEST HANDLER: failed to connect guest\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isChanged() {
        return false;
    }

    private boolean connectGuest(String firstLine) {
        int id;
        if ((id = gameManager.connectMe(firstLine)) != 0) {
            this.GUEST_ID = id;
            this.QUIT_GAME_ID = GUEST_ID + ",quitGame,true"; // quit game modifier
            this.YOUR_TURN_ID = GUEST_ID + ",isMyTurn,"; // Add true or false
            this.CONNECTION_ID = id + ",connectMe,true"; // ack & id
            return true;
        } else
            // PRINT DEBUG
            System.out.println("GUEST HANDLER: faild to connect guest\n");
        return false;
    }

    private boolean isGuestTurn() {
        return gameManager.getPlayerByID(GUEST_ID).isMyTurn();
    }

    @Override
    public void close() {
        // Cleanup or additional close logic if required

    }
}
