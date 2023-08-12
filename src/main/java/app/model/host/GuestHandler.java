package app.model.host;

import java.io.*;

import app.model.MethodInvoker;
import app.model.game.GameManager;
import app.model.server.ClientHandler;
import app.view_model.MessageReader;

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

public class GuestHandler implements ClientHandler {

    private final GameManager gameManager;
    private BufferedReader in;
    private PrintWriter out;
    private int myId;
    private String quitGameString;

    public GuestHandler() {
        this.gameManager = GameManager.get();
    }

    @Override
    public void handleClient(InputStream inputStream, OutputStream outputStream) {
        try {
            this.in = new BufferedReader(new InputStreamReader(inputStream));
            this.out = new PrintWriter(outputStream, true);

            connectGuest();
            waitingRoom(); // Waiting for all the players to choose book and set Ready
            startChat(); // Starts a chat with the player until quitGame string

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void connectGuest() throws IOException {
        String message = in.readLine();
        String[] params = message.split(",");
        if (params[0].equals("0") && params[1].equals(MethodInvoker.connectMe)) {
            String name = params[2];
            this.myId = gameManager.connectGuestHandler(name);
            this.quitGameString = myId + "," + MethodInvoker.quitGame + "," + "true"; // quit game modifier
            // this.yourTurnString = myId + ",isMyTurn,true"; // my turn modifier
            String connectionMessage = myId + "," + MethodInvoker.connectMe + "," + myId; // ack & id
            out.println(connectionMessage); // send id
            // PRINT DEBUG
            System.out.println("GUEST HANDLER: guest " + myId + " connected!\n");
        } else {
            // PRINT DEBUG
            System.out.println("GUEST HANDLER: failed to connect guest\n");
        }
    }

    private void waitingRoom() throws IOException, InterruptedException {
        boolean isBooksSet = false, ready = false;

        while (!gameManager.isReadyToPlay()) {
            String message;
            if (!isBooksSet) {
                message = in.readLine();
                String[] params = message.split(",");
                int id = Integer.parseInt(params[0]);
                if (id == myId && params[1].equals(MethodInvoker.myBooksChoice)) {
                    String bookList = params[2];
                    String ans = gameManager.addBooksHandler(bookList);
                    if (ans.equals("true")) {
                        isBooksSet = true;
                        out.println(myId + "," + MethodInvoker.myBooksChoice + "," + ans);
                        // PRINT DEBUG
                        System.out.println("GUEST HANDLER: guest " + myId + " set book choice! \n");
                    } else {
                        out.println(myId + ",myBookChoice," + ans);
                        // PRINT DEBUG
                        System.out.println("GuestHandler: cant set guest's book");
                    }
                }
                if (!ready) {
                    message = in.readLine();
                    params = message.split(",");
                    id = Integer.parseInt(params[0]);
                    if (id == myId && params[1].equals(MethodInvoker.ready)) {
                        if (params[2].equals("true")) {
                            gameManager.setReady();
                            // PRINT DEBUG
                            System.out.println("GUEST HANDLER: guest " + myId + " is ready to play!\n");
                        } else {
                            // PRINT DEBUG
                            System.out.println("GuestHandler: cant set guest's ready val");
                        }
                    }
                }

            } else
                Thread.sleep(3000);
        }
    }

    private void startChat() throws IOException {
        String guestMessage;
        // START CHAT
        while (!(guestMessage = in.readLine()).equals(quitGameString)) {
            String[] params = guestMessage.split(",");
            int messageId = Integer.parseInt(params[0]);
            String modifier = params[1];
            String value = params[2];

            // Check if it's currect ID, if not drops the message
            if (messageId == myId) {
                // Process guest's instructions
                String returnValue = gameManager.processPlayerInstruction(myId, modifier, value);
                // Send response to the guest
                String response = myId + "," + modifier + "," + returnValue;
                out.println(response);
            }
        }
        // Guest chose to quit game - chat ended
        String playerName = this.gameManager.getPlayerByID(myId).getName();
        out.println(quitGameString);
        gameManager.quitGameHandler(quitGameString);
        MessageReader.setMsg(playerName + " has quit the game!");
        // PRINT DEBUG
        System.out.println("GUEST HANDLER: chat ended, " + myId + " has quit the game\n");
    }

    @Override
    public void close() throws IOException {
        in.close();
        out.close();
    }
}
