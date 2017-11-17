package back.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThread implements Runnable {

    private final String TERMINATE_COMMAND = ".terminate";
    private Socket clientSocket = null;
    private int clientID;
    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean isStopped = false;

    //TODO Create a handler to communicate between Server and ServerThread

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
            Thread.sleep(200);

            //Initialing the inputs and outputs
            in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            out = new PrintWriter(this.clientSocket.getOutputStream(), true);
            String message;

            //Sending welcoming message to client
            out.println("Welcome to the server:");
            out.println(clientID);
            out.flush();

            //TODO Process information

            //Reading information
            while (!isStopped) {
                if ((message = in.readLine()) != null) {
                    if (message.equals(TERMINATE_COMMAND)) {
                        //Request to terminate will be granted.
                        if (terminateConnection(Server.TO_SERVER)) {
                            //TODO Handler inform
                        } else {
                            //TODO Handler inform error
                        }
                        break;
                    }

                    System.out.println(message);
                    System.out.flush();
                }
            }

        } catch (InterruptedException e) {
            // We've been interrupted: no more messages.

        } catch (IOException e) {
            //TODO Handler
            e.printStackTrace();
        }

        terminateConnection(Server.TO_CLIENT);
    }


    /**
     * Close the connections between the source and the destination
     *
     * @param destination The destination of which the source is trying to sever connections
     * @return Validation if termination was success
     */
    public boolean terminateConnection(int destination) {
        try {
            if (destination == Server.TO_CLIENT) {
                this.out.println(".terminate");
            }

            this.in.close();
            this.out.close();
            this.clientSocket.close();
            //TODO Handler will inform server
            isStopped = true;
            return isStopped;
        } catch (IOException e) {
            //TODO Handler "Error on closing client connection");
        }

        return isStopped;
    }

    //TODO Figure out how to inform Server of client commands
}
