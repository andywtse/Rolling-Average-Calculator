package back.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * This class is used to create a network connection to the {@link Server}
 * for a user to enter numbers.
 */
public class Client {

    BufferedReader in;
    PrintWriter out;

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
            //TODO Handler
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
    public void connect(final String ipAddress, final String port) {
        System.out.println("Attempting to connect to " + ipAddress
            + ":" + port);

        new Thread(()-> {
            try {
                Socket clientSocket = new Socket();
                clientSocket.connect(new InetSocketAddress(ipAddress, Integer.parseInt(port)), 2000);
                UIHandler.onConnectionSuccess();

                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                UIHandler.onConnectionFailure("Failed to open socket");
            }
        }).start();




//        // FIXME 11/07/17: Fill in method and remove the random calls.
//        if (UIHandler != null) {
//            new Thread(() -> {
//                // Simulate a network delay.
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                final boolean didConnect =  Math.random() > 0.99f;
//                if (didConnect) {
//                    UIHandler.onConnectionSuccess();
//                } else {
//                    UIHandler.onConnectionFailure(
//                        "You were unlucky, I guess.");
//                }
//            }).start();
//        } else {
//            System.err.println("Attempted to connect without user interface.");
//            System.exit(-1);
//        }
    }

    /**
     * Opens the client socket with designated port and address
     */
    private void openClientSocket() {
        try {
            this.clientSocket = new Socket();
            this.clientSocket.connect(new InetSocketAddress(clientAddress, clientPort), 2000);
        } catch (IOException e) {
            //TODO Handler
//            UIHandler.onConnectionFailure("Failed to open socket");
            System.out.println("Failed to open socket");
        }
    }
}
