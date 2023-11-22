package app.model.host;

import java.io.*;

import app.model.GetMethod;
import app.model.game.GameManager;
import app.model.server.ClientHandler;

/*
 * The GuestHandler is responsible for the communication between the host and the guests.
 * The communication is done using strings:
 * 
 * HOST:
 * receives a string from the guest
 * starting with the guests ID,
 * then a model method to apply,
 * and then the value (etc. query word).
 * All 3 parameters seperated by ","
 * 
 * e.g. - "0,connectMe,Moshe" , "0,getMyID,Moshe" , "259874,tryPlaceWord,Hello"
 * (ID is 0 for initialization)
 * 
 * GUEST:
 * recieves a string from the host
 * starting with his ID,
 * then the model methos that was applied
 * and then the returned value.
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
    private int MY_ID;
    private String QUIT_GAME;
    private boolean flag;

    public GuestHandler() {
        this.gameManager = GameManager.get();
    }

    @Override
    public void handleClient(InputStream inputStream, OutputStream outputStream) {
        try {
            this.in = new BufferedReader(new InputStreamReader(inputStream));
            this.out = new PrintWriter(outputStream, true);

            connectGuest();
            if (!flag) {
                waitingRoom(); // Waiting for all the players to choose book and set Ready
                if (!flag) {
                    startChat(); // Starts a chat with the player until QUIT_GAME string
                }
            } else {
                close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connectGuest() throws Exception {
        String message = in.readLine();
        String[] params = message.split(",");
        if (params[0].equals("0") && params[1].equals(GetMethod.connectMe)) {
            String name = params[2];
            this.MY_ID = gameManager.connectGuestHandler(name);
            flag = MY_ID == 0 ? true : false;
            this.QUIT_GAME = MY_ID + "," + GetMethod.quitGame + "," + "true"; // quit game modifier
            String connectionMessage = MY_ID + "," + GetMethod.connectMe + "," + MY_ID; // ack & id
            out.println(connectionMessage); // send id
            flag = false;
        } else {
        }
    }

    private void waitingRoom() throws Exception {
        boolean isBooksSet = false, ready = false;

        while (!gameManager.isReadyToPlay()) {
            String message;
            if (!isBooksSet) {
                message = in.readLine();
                String[] params = message.split(",");
                int id = Integer.parseInt(params[0]);
                if (id == MY_ID && params[1].equals(GetMethod.myBooksChoice)) {
                    String bookList = params[2];
                    String ans = gameManager.addBooksHandler(bookList);
                    if (ans.equals("true")) {
                        isBooksSet = true;
                        out.println(MY_ID + "," + GetMethod.myBooksChoice + "," + ans);
                    } else {
                        out.println(MY_ID + ",myBookChoice," + ans);
                    }
                }
                if (!ready) {
                    message = in.readLine();
                    params = message.split(",");
                    id = Integer.parseInt(params[0]);
                    if (id == MY_ID && params[1].equals(GetMethod.ready)) {
                        if (params[2].equals("true")) {
                            gameManager.setReady();
                            break;
                        } else {
                        }
                    }
                }

            } else {
                Thread.sleep(3000);
            }
        }
    }

    private void startChat() throws Exception {
        String guestMessage;
        // START CHAT
        while (!(guestMessage = in.readLine()).equals(QUIT_GAME)) {
            String[] params = guestMessage.split(",");
            int messageId = Integer.parseInt(params[0]);
            String modifier = params[1];
            String value = params[2];

            // Check if it's currect ID, if not drops the message
            if (messageId == MY_ID) {
                // Process guest's instructions
                String returnValue = gameManager.processPlayerInstruction(MY_ID, modifier, value);
                // Send response to the guest
                String response = MY_ID + "," + modifier + "," + returnValue;
                out.println(response);
            }
        }
        // Guest chose to quit game - chat ended
        String playerName = this.gameManager.getPlayerByID(MY_ID).getName();
        // Sent quit game query to the guest
        out.println(QUIT_GAME);
        // Handle quit in game manager
        gameManager.quitGameHandler(QUIT_GAME);
    }

    @Override
    public void close() throws IOException {
        in.close();
        out.close();
    }
}