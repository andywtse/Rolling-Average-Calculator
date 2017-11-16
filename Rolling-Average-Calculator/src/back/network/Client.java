package back.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client implements Runnable {

    private PrintWriter out;
    private BufferedReader in;

    private int clientPort;
    private String clientAddress;
    private Socket clientSocket;

    private int clientID;

    /**
     * Creates new Client with address and port
     *
     * @param address The IPv4 or IPv6 address to create the network server.
     * @param port    The port number to open for the network server.
     */
    public Client(String address, int port) {
        this.clientPort = port;
        this.clientAddress = address;
    }

    public void messageToServer(String message) {
        this.out.println(message);
        this.out.flush();
    }

    /**
     * Opens the socket and establishes IO with server
     */
    public void run() {

        openClientSocket();

        try {
            this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            this.out = new PrintWriter(this.clientSocket.getOutputStream(), true);

            System.out.println(in.readLine());
            System.out.println(clientID = Integer.parseInt(in.readLine()));

            out.print("Hello");
            out.flush();

            //TODO Create a listener to receive messages from the server


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void messageToServer(String message) {
        this.out.println(message);
        this.out.flush();
    }

    /**
     * Shutdowns the client connection with the server
     */
    public void terminate() {
        try {
            this.in.close();
            this.out.close();

            //TODO Send message to server to close this client with this ID

            this.clientSocket.close();
            if (clientSocket.isClosed()) {
                //TODO
                //UIHandler.onShutdownSuccess();
            }
        } catch (IOException e) {
            //TODO
            //UIHandler.onShutdownFailure("Error in closing connection");
            System.out.println("Error in closing connection");
        }
    }

    /**
     * Opens the client socket with designated port and address
     */
    private void openClientSocket() {
        try {
            this.clientSocket = new Socket();
            this.clientSocket.connect(new InetSocketAddress(clientAddress, clientPort), 2000);
        } catch (IOException e) {
            //TODO
//            UIHandler.onConnectionFailure("Failed to open socket");
            System.out.println("Failed to open socket");
        }
    }
}
