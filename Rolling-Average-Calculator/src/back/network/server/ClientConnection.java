package back.network.server;

import utility.request.Request;
import utility.request.RequestFactory;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import static back.network.client.ClientAdapter.WAIT_DELAY_MS;
import static utility.request.Request.Response.OK;
import static utility.request.Request.Topic.DISCONNECT;

public class ClientConnection implements Runnable {
    
    
    private static final int TIMEOUT_DELAY_MS = 2000;
    private Socket clientSocket = null;
    private long clientID;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private volatile boolean isStopped = false;
    private ClientConnectionHandler serverCCHandler;
    private Request request;
    private Thread clientConnectionThread;
    
    private List<Integer> clientSubmission = new ArrayList<>();

    //TODO Create a handler to communicate between Server and ClientConnection

    /**
     * Creates new ClientConnection with a clientSocket
     *
     * @param clientSocket
     */
    public ClientConnection( Socket clientSocket, long clientID) {
        this.clientSocket = clientSocket;
        this.clientID = clientID;
    }
    
    /**
     * Establish a link to the Server
     *
     * @param handler The communication interface being used.
     */
    public void setServerCCHandler(final ClientConnectionHandler handler) {
        serverCCHandler = handler;
    }
    
    /**
     * Creates IO channel in the sockets to allow communication between
     * clients and the server
     */
    public void run() {
        
        try {
            Thread.sleep(200);
            //Initialing the inputs and outputs
            
            this.out = new ObjectOutputStream(this.clientSocket.getOutputStream());

            //Sending ID to client
            this.out.writeLong(clientID);
            this.out.flush();
            
            this.in = new ObjectInputStream(this.clientSocket.getInputStream());
            this.clientSocket.setSoTimeout(TIMEOUT_DELAY_MS);
            
            
            //TODO Process information

            //Reading information
            Runnable clientConnectionTask = () -> {
                try {
                    while (!this.isStopped) {
                        try {
                            if (( this.request = (Request) this.in.readObject() ) != null) {
        
                                //TODO Process Submit, Average(self), and History(self) differently, otherwise, go straight to server
        
                                serverCCHandler.onRequestReceived(request);
        
                                //TODO More requests handling here
        
                                // Theoretically, the ClientAdapter to validate the inputs to be only numbers
                                // Should there be any non-numbers, it would mean its commands to request informing or write
                                // So any other messages received by ClientConnection should be submitted as a request to add
                                // to the sum
        
                            }
                        }  catch ( SocketTimeoutException e) {
                            // Restart read
                        }
                    }
                } catch (SocketException e){
                    //TODO Handler
                    //ServerSocket was closed
                    e.printStackTrace();
                } catch (ClassNotFoundException e){
                    //SHOULD NEVER HAPPEN
                    e.printStackTrace();
                } catch (IOException e){
                    //TODO Handler
                    e.printStackTrace();
                }
            };
            clientConnectionThread = new Thread(clientConnectionTask);
            clientConnectionThread.start();
            
        } catch (InterruptedException e) {
            // We've been interrupted: no more messages.
            return;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Close the connections between the source and the destination
     *
     * @return Validation if termination was success
     */
    public synchronized boolean terminateConnection() {
        try {
            Request disconnectClient = RequestFactory.serverDisconnect();
            this.out.writeObject(disconnectClient);
            this.out.flush();
    
            try{
                this.wait(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            this.in.close();
            this.out.close();
            this.clientSocket.close();
            this.clientConnectionThread.interrupt();
            
            this.isStopped = true;
            return this.isStopped;
        } catch (IOException e) {
            //TODO Handler "Error on closing client connection");
            e.printStackTrace();
        }

        return this.isStopped;
    }
    
    public void respondToClient(Request response){
    
    }
    
    public interface ClientConnectionHandler{
        void onRequestReceived( Request request );
        void onRequestSubmissionFailure( String reason );
    }
}
