package back.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThread implements Runnable {

    private Socket clientSocket = null;
    private int clientID;

    /**
     * Creates new ServerThread with a clientSocket
     *
     * @param clientSocket
     */
    public ServerThread(Socket clientSocket, int clientID) {
        this.clientSocket = clientSocket;
        this.clientID = clientID;
    }

    /**
     * Creates IO channel in the sockets to allow communication between
     * clients and the server
     */
    public void run() {

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(this.clientSocket.getOutputStream(), true);
            String message;

            out.println("Welcome to the server:");
            out.println(clientID);
            out.flush();

            //TODO Process information

            while (true) {
                if ((message = in.readLine()) != null) {
                    System.out.println(message);
                    System.out.flush();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            this.clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error on closing client connection");
        }
    }

    //TODO Figure out how to inform Server of client commands
}
