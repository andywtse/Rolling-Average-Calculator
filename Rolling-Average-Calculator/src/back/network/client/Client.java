package back.network.client;

import utility.request.Request;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client implements Runnable {
    
    private static final int TIMEOUT_DELAY_MS = 2000;
    
    private ClientHandler CCHandler;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    private String clientAddress;
    private int clientPort;
    private long clientID;
    private volatile boolean isStopped = false;
    
    private Socket clientSocket;
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
    public void setCCHandler( final ClientHandler handler ) { CCHandler = handler; }
    
    /**
     * The {@link Client} will call openClientSocket() to establish a connection with the {@link back.network.server.Server}.
     * Then it initializes the inputs and outputs to allow communication between the client and server.
     * Afterwards, it will create a listening thread to read responses from the server.
     */
    public void run() {
        
        openClientSocket();
        
        try {
            Thread.sleep(200);
            
            this.out = new ObjectOutputStream(this.clientSocket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(this.clientSocket.getInputStream());
            
            this.clientID = this.in.readLong();
            this.CCHandler.onClientIdObtained(this.clientID);
            
            this.clientThread = new Thread(this::responseFromServer);
            this.clientThread.start();
            
            
        } catch (InterruptedException e) {
            // We've been interrupted: no more messages.
            return;
            
        } catch (IOException e) {
            CCHandler.onIOSocketFailure("IO Brokeded");
        }
        
    }
    
    /**
     * Attempts to read response from {@link back.network.server.Server} by reading a JSON string
     * and building it to a {@link Request}. It will then inform the user of the response from the
     * server about their request.
     */
    private void responseFromServer(){
        
        String jsonOutput;
        
        while(!isStopped){
            try {
                jsonOutput = (String)this.in.readObject();
                if (jsonOutput != null) {
                    Request response = new Request.Builder().fromJSONString(jsonOutput).build();
    
                    switch (response.getTopic()) {
                        case AVERAGE:
                            System.out.println("Average: " + response.getAmount());
                            System.out.flush();
                            break;
                        case COUNT:
                            System.out.println("Count: " + response.getAmount());
                            System.out.flush();
                            break;
                        case HISTORY:
                            System.out.println("History: " + response.getEntries());
                            System.out.flush();
                            break;
                        case USERS:
                            System.out.println("Users: " + response.getAmount());
                            System.out.flush();
                            break;
                    }
                    
                }else{
                    //TODO Handle bad response
                }
            } catch (EOFException e){
                //TODO Figure out how to handle this
            } catch (ClassNotFoundException e){
                //TODO Figure out how to handle this
            } catch (IOException e){
                CCHandler.onIOSocketFailure("Could not receive response from server");
            }
        }
    }
    
    /**
     * Writes the {@link Request} JSON String to the outputstream of Client.
     *
     * @param request The Request that has been made by the client
     */
    public void requestToServer( Request request) {
        
        if(request==null){
            CCHandler.onRequestFailure("The request is null.");
            return;
        }
        
        try {
            this.out.writeObject(request.toJSONString());
        } catch (IOException e){
            CCHandler.onIOSocketFailure("Could not send request to server");
        }
    }
    
    /**
     * Attempts to terminate the {@link Client} by closing the input and output streams. Then attempting
     * to close the socket.
     */
    public boolean shutdown() {
        
        try {
            this.in.close();
            this.out.close();
            this.isStopped = true;
            
            this.clientSocket.close();
            if (clientSocket.isClosed()) {
                //TODO Requires disconnect
                CCHandler.onShutdownSuccess();
            }
        } catch (IOException e) {
            CCHandler.onShutdownFailure("Error in closing client");
        }
        
        return this.isStopped;
    }
    
    /**
     * Attempts to create a new socket and connect with address and port with a timeout of 5 seconds.
     * Should the connection fail, it will callback to {@link ClientAdapter} with the issue.
     */
    private void openClientSocket() {
        
        try {
            this.clientSocket = new Socket();
            this.clientSocket.connect(new InetSocketAddress(clientAddress, clientPort), 5000);
            CCHandler.onOpenSocketSuccess();
        } catch (IOException e) {
            CCHandler.onOpenSocketFailure("Could not open socket of IP: " + clientAddress + " and Port: " + clientPort);
        }
    }
    
    /**
     * @return Client ID, Client Address, and Client Port
     */
    @Override
    public String toString() {
        
        return "ClientID: " + clientID + "\nClient Address: " + clientAddress + "\nClient Port: " + clientPort;
    }
    
    /**
     * The communication interface for the Client to the {@link ClientAdapter}.
     */
    public interface ClientHandler {
    
        /**
         * Callback to the {@link ClientAdapter} to inform success of opening socket.
         */
        void onOpenSocketSuccess();
    
        /**
         * Callback to the {@link ClientAdapter} to inform failure of opening socket.
         * Example reason include port or ip being invalid or used
         */
        void onOpenSocketFailure( final String reason );
    
        /**
         * Callback to the {@link ClientAdapter} to inform connection to the server.
         */
        void onServerConnected( final String address);
    
        /**
         * Callback to the {@link ClientAdapter} to inform connection to server has been broken.
         * Example reason include the {@link back.network.server.ClientConnection} abruptly closing
         * connection but {@link Client} still retains the connection.
         */
        void onConnectionBroken( final String reason);
    
        /**
         * Callback to the {@link ClientAdapter} to inform success on shutdown
         * {@link ClientAdapter} will then process the terminated client
         */
        void onShutdownSuccess();
    
        /**
         * Callback to the {@link ClientAdapter} to inform failure on shutdown.
         * Example reason of failure include the sockets or threads not closing.
         */
        void onShutdownFailure(final String reason);
        
        /**
         * Callback to the {@link ClientAdapter} to inform failure in client socket. Example reason of
         * failure include the input or output stream abruptly closed and cannot write or read from them.
         */
        void onIOSocketFailure( final String reason );
    
        /**
         * Callback to the {@link ClientAdapter} to inform the new clientID of the user.
         * Used to create Requests in the {@link ClientAdapter}
         */
        void onClientIdObtained( final long id );
    
        /**
         * Callback to the {@link ClientAdapter} to inform the an issue with the request.
         * Example reason include the request being null. Extreme case!
         */
        void onRequestFailure( final String reason);
    }
}
