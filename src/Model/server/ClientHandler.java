package model.Server;

import java.io.*;

public interface ClientHandler {
	void handleClient(InputStream inFromclient, OutputStream outToClient);
	void close();
}
