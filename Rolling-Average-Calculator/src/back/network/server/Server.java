package back.network.server;

import utility.request.Request;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Server implements Runnable, ClientConnection.ClientConnectionHandler {
    
    private static final int TIMEOUT_DELAY_MS = 2000;
    private static final int WAIT_DELAY_MS = 1000;
    
    private ServerHandler SSHandler;
    
    private ServerSocket serverSocket = null;
    private ExecutorService threadPool = Executors.newFixedThreadPool(4);
    
    private volatile boolean isStopped = false;
    private String serverAddress;
    private int serverPort;
    
    private Map<Long, ClientConnection> clientIdConnection = new HashMap<Long, ClientConnection>();
    private AtomicLong clientId = new AtomicLong(0);
    private List<Integer> listOfClientSubmission = new ArrayList<>();
    
    private int count = 0;
    private double sum = 0;
    private double average;
    
    /**
     * Creates new Server with address and port
     *
     * @param address The IPv4 or IPv6 address to create the network server.
     * @param port    The port number to open for the network server.
     */
    public Server( String address, int port ) {
        
        this.serverPort = port;
        this.serverAddress = address;
    }
    
    /**
     * Establish a link to the ServerAdapter
     *
     * @param handler The communication interface being used.
     */
    public void setSSHandler( final ServerHandler handler ) {
        
        SSHandler = handler;
    }
    
    /**
     * Creates new thread for every new connection from clients
     */
    @Override
    public void run() {
        
        openServerSocket();
        SSHandler.onOpenSocketSuccess();
        ClientConnection clientConnection;
        
        while (!isStopped) {
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (SocketTimeoutException e) {
                // Restart accept
            } catch (IOException e) {
                if (isStopped) {
                    System.out.println("ServerAdapter has stopped");
                    break;
                }
                SSHandler.onOpenSocketFailure("Could not open clientSocket");
                this.isStopped = true;
                break;
            }
            
            if (clientSocket != null) {
                // Puts the ID and Address into a map
                long curClientID = clientId.getAndIncrement();
                
                
                // Creates the thread and puts it into a list of server threads
                // After, it saves reference a Future of the thread so it can be distinct within the threadPool since we do not own it
                SSHandler.onClientConnected(clientSocket.getLocalAddress().toString(), curClientID);
                clientConnection = new ClientConnection(clientSocket, curClientID);
                clientConnection.setServerCCHandler(this);
                clientIdConnection.put(curClientID, clientConnection);
                Thread clientConnectionThread = new Thread(clientConnection);
                this.threadPool.execute(clientConnectionThread);
                
            }
        }
        
    }
    
    /**
     * Shutdown the Server by closing the thread pool and closing
     * the sockets of Client and Servers
     */
    public synchronized boolean terminate() {
        
        //Shutdown each clientSocket gracefully by alerting each client
        for (ClientConnection curClientConnection : clientIdConnection.values()) {
            try {
                curClientConnection.terminateConnection();
                this.wait(WAIT_DELAY_MS);
            } catch (InterruptedException e) {
                SSHandler.onShutdownFailure("ClientConnection shutdown was interrupted ");
            }
        }
        
        
        //Tries to close:
        // Server socket
        // ThreadPool
        try {
            this.isStopped = true;
            this.serverSocket.close();
            
            this.threadPool.shutdown();
            
            this.threadPool.awaitTermination(60000, TimeUnit.NANOSECONDS);
            if (threadPool.isTerminated()) {
                SSHandler.onShutdownSuccess();
            } else {
                SSHandler.onShutdownFailure("Thread pool failed to terminate");
                isStopped = false;
            }
            
        } catch (IOException e) {
            SSHandler.onShutdownFailure("IOException: Error closing server");
        } catch (InterruptedException e) {
            SSHandler.onShutdownFailure("Server shutdown was interrupted ");
        }
        
        
        return this.isStopped;
    }
    
    /**
     * Opens the server socket with designated port and address
     */
    private void openServerSocket() {
        
        try {
            
            InetAddress address = InetAddress.getByName(this.serverAddress);
            this.serverSocket = new ServerSocket(this.serverPort, 50, address);
            this.serverSocket.setSoTimeout(TIMEOUT_DELAY_MS);
            
        } catch (UnknownHostException e) {
            SSHandler.onOpenSocketFailure("Could not get host: " + serverAddress);
        } catch (SocketException e) {
            SSHandler.onOpenSocketFailure("Could not set the timeout delay for serverSocket");
        } catch (IOException e) {
            SSHandler.onOpenSocketFailure("Could not open server port " + serverPort);
        }
        
    }
    
    /**
     * Takes the response from Server Thread Handler and interrupts the thread that holds ClientID
     *
     * @param clientID Integer value unique to the client
     */
    private void terminateThread( long clientID ) {
        //TODO Take response from handler and close thread with clientID
        
        if (clientIdConnection.containsKey(clientID)) {
            try {
                clientIdConnection.get(clientID).terminateConnection();
                this.wait(WAIT_DELAY_MS);
                clientIdConnection.remove(clientID);
                System.out.println("Dunzo");
            } catch (InterruptedException e) {
                SSHandler.onShutdownFailure("ClientConnection shutdown was interrupted ");
            }
        }
        
        
    }
    
    @Override
    public synchronized void onRequestReceived( Request request ) {
        
        //TODO Fill this in
        switch (request.getTopic()) {
            case SUBMIT:
                break;
            case AVERAGE:
                break;
            case COUNT:
                break;
            case HISTORY:
                break;
            case USERS:
                break;
            case DISCONNECT:
                break;
        }
        
    }
    
    /**
     * Handler to communicate with ServerAdapter
     */
    public interface ServerHandler {
        
        void onOpenSocketSuccess();
        
        void onOpenSocketFailure( final String reason );
        
        void onClientConnected( final String ipAddress, final long clientID );
        
        void onClientDisconnected( final String ipAddress, final long clientID );
        
        void onShutdownSuccess();
        
        void onShutdownFailure( final String reason );
        
        void onConnectionBroken( final String reason );
        
    }
}
