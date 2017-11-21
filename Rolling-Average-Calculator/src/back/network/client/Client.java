package back.network.client;

import utility.request.Request;
import utility.request.RequestFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Client implements Runnable {
    
    private static final int TIMEOUT_DELAY_MS = 2000;
    private final String COMMAND_DISCONNECT = ".disconnect";
    private final String COMMAND_SUBMIT = ".submit";
    private final String COMMAND_COUNT = ".count";
    private final String COMMAND_HISTORY = ".history";
    private final String COMMAND_AVERAGE = ".average";
    private final String COMMAND_USER = ".user";
    private ClientHandler CCHandler;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private int clientPort;
    private String clientAddress;
    private Socket clientSocket;
    private long clientID;
    private volatile boolean isStopped = false;
    private Thread clientThread;
    
    /**
     * Creates new Client with address and port
     *
     * @param address The IPv4 or IPv6 address to create the network server.
     * @param port    The port number to open for the network server.
     */
    public Client( String address, int port ) {
        
        this.clientPort = port;
        this.clientAddress = address;
    }
    
    /**
     * Establish a link to the communication interface that the user
     * will use.
     *
     * @param handler The communication interface being used.
     */
    public void setCCHandler( final ClientHandler handler ) {
        
        CCHandler = handler;
    }
    
    /**
     * Opens the socket and establishes IO with server
     */
    public void run() {
        
        openClientSocket();
        
        try {
            Thread.sleep(200);
            
            this.out = new ObjectOutputStream(this.clientSocket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(this.clientSocket.getInputStream());
            this.clientSocket.setSoTimeout(TIMEOUT_DELAY_MS);
            
            this.clientID = this.in.readLong();
            System.out.println("\nClient ID: " + this.clientID);
            System.out.flush();
            
            
            //TODO Create a listener to receive messages from the server
            //TODO Figure out best way to make separate threads for read/write
            
            Runnable clientReadTask = () -> {
                
                Request responseFromServer;
                
                try {
                    
                    Thread.sleep(200);
                    while (!isStopped) {
                        try {
                            if (( responseFromServer = (Request) this.in.readObject() ) != null) {
                                
                                //TODO PROCESS REQUEST more indepth
                                if (responseFromServer.getTopic().equals(Request.Topic.DISCONNECT)) {
                                    System.out.println("Request to terminate beginning...");
                                    System.out.flush();
                                    terminate();
                                } else if (responseFromServer.getTopic().equals(Request.Topic.AVERAGE)) {
                                    System.out.println("Average: " + responseFromServer.getAmount());
                                    System.out.flush();
                                } else if (responseFromServer.getTopic().equals(Request.Topic.COUNT)) {
                                    System.out.println("Count: " + responseFromServer.getAmount());
                                    System.out.flush();
                                } else if (responseFromServer.getTopic().equals(Request.Topic.HISTORY)) {
                                    System.out.println("Number of submission: " + responseFromServer.getAmount());
                                    System.out.flush();
                                }
                            }
                        } catch (SocketTimeoutException e) {
                            // Restart read
                        } catch (EOFException e) {
                            //Reached End Of File, do stuff
                        }
                        
                    }
                    
                    System.out.println("Disconnected from the server");
                } catch (ClassNotFoundException e) {
                    //SHOULD NOT HAPPEN
                    terminate();
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // We've been interrupted: no more messages.
                    return;
                } catch (IOException e) {
                    CCHandler.onIOSocketFailure("IO Seized to function");
                }
            };
            
            this.clientThread = new Thread(clientReadTask);
            this.clientThread.start();
            
            
        } catch (InterruptedException e) {
            // We've been interrupted: no more messages.
            return;
            
        } catch (IOException e) {
            CCHandler.onIOSocketFailure("IO Brokeded");
        }
        
    }
    
    /**
     * Sends a message to the server
     *
     * @param command String message to be sent
     */
    public void commandToServer( String command, String range ) {
        
        Request requestToServer;
        Request.Range commandRange;
        if (range.equals(".all")) {
            commandRange = Request.Range.ALL;
        } else {
            commandRange = Request.Range.SELF;
        }
        
        switch (command) {
            case COMMAND_DISCONNECT:
                try {
                    requestToServer = RequestFactory.clientDisconnect(clientID);
                    this.out.writeObject(requestToServer);
                    this.out.flush();
                } catch (IOException e) {
                    CCHandler.onIOSocketFailure("Could not write requestToServer");
                }
                break;
            case COMMAND_COUNT:
                try {
                    requestToServer = RequestFactory.clientCountRequest(clientID, commandRange);
                    this.out.writeObject(requestToServer);
                    this.out.flush();
                } catch (IOException e) {
                    CCHandler.onIOSocketFailure("Could not write requestToServer: Count");
                }
                break;
            case COMMAND_AVERAGE:
                try {
                    requestToServer = RequestFactory.clientAverageRequest(clientID, commandRange);
                    this.out.writeObject(requestToServer);
                    this.out.flush();
                } catch (IOException e) {
                    CCHandler.onIOSocketFailure("Could not write requestToServer: Average");
                }
                break;
            case COMMAND_HISTORY:
                try {
                    requestToServer = RequestFactory.clientHistoryRequest(clientID, commandRange);
                    this.out.writeObject(requestToServer);
                    this.out.flush();
                } catch (IOException e) {
                    CCHandler.onIOSocketFailure("Could not write requestToServer: History");
                }
                break;
        }
    }
    
    /**
     * Sends the value to the server
     *
     * @param value value of number
     */
    public void valueToServer( int value ) {
        
        Request requestToServer;
        
        requestToServer = RequestFactory.clientSubmitRequest(clientID, value);
        System.out.println(requestToServer.toJSONString());
        System.out.flush();
        
        try {
            this.out.writeObject(requestToServer);
            this.out.flush();
        } catch (IOException e) {
            CCHandler.onIOSocketFailure("Could not write requestToServer");
        }
    }
    
    /**
     * Shutdowns the client connection with the server
     */
    public boolean terminate() {
        
        try {
            this.in.close();
            this.out.close();
            
            System.out.println("HERE BABY");
            System.out.flush();
            
            this.clientSocket.close();
            if (clientSocket.isClosed()) {
                CCHandler.onConnectionBroken("Client or Server severed connection");
                this.isStopped = true;
            }
        } catch (IOException e) {
            CCHandler.onShutdownFailure("Error in closing client");
        }
        
        return this.isStopped;
    }
    
    /**
     * Opens the client socket with designated port and address
     */
    private void openClientSocket() {
        
        try {
            this.clientSocket = new Socket();
            this.clientSocket.connect(new InetSocketAddress(clientAddress, clientPort), 2000);
            CCHandler.onOpenSocketSuccess();
        } catch (IOException e) {
            CCHandler.onOpenSocketFailure("Could not open socket of IP: " + clientAddress + " and Port: " + clientPort);
        }
    }
    
    /**
     * @return Client ID, Client Address, and Client Port
     */
    public String toString() {
        
        return "ClientID: " + clientID + "\nClient Address: " + clientAddress + "\nClient Port: " + clientPort;
    }
    
    /**
     * Handler to communicate between ClientAdapter and Client
     */
    public interface ClientHandler {
        
        void onOpenSocketSuccess();
        
        void onOpenSocketFailure( final String reason );
        
        void onServerConnected( final String ipAddress );
        
        void onClientDisconnected( final String ipAddress, final long clientID );
        
        void onShutdownSuccess();
        
        void onShutdownFailure( final String reason );
        
        void onConnectionBroken( final String reason );
        
        void onIOSocketFailure( final String reason );
    }
}
