package back.network.server;

import utility.request.Request;
import utility.request.RequestFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

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
    public ClientConnection( Socket clientSocket, long clientID ) {
        
        this.clientSocket = clientSocket;
        this.clientID = clientID;
    }
    
    /**
     * Establish a link to the Server
     *
     * @param handler The communication interface being used.
     */
    public void setServerCCHandler( final ClientConnectionHandler handler ) {
        
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
                                System.out.println("Request has been read: "+this.request.toJSONString());
                                System.out.flush();
                                
                                if (Request.Topic.valueOf(this.request.getTopic()).equals(Request.Topic.SUBMIT)) {
                                    System.out.println("Response: SUBMIT");
                                    System.out.flush();
                                    this.clientSubmission.add(this.request.getAmount());
                                    this.serverCCHandler.onRequestReceived(request);
                                    
                                } else if (Request.Topic.valueOf(this.request.getTopic()).equals(Request.Topic.AVERAGE)) {
    
                                    System.out.println("Response: AVERAGE");
                                    System.out.flush();
                                    if (Request.Range.valueOf(this.request.getRange()).equals(Request.Range.SELF)) {
                                        if(clientSubmission.size()==0){
                                            System.out.println("Average: " + 0);
                                            respondToClient(RequestFactory.serverAverageResponse(Request.Response.OK, Request.Range.SELF, 0));
                                        }else {
                                            int average = 0;
                                            for (int curValue : clientSubmission) {
                                                average += curValue;
                                            }
                                            System.out.println("Average: " + average / clientSubmission.size());
                                            respondToClient(RequestFactory.serverAverageResponse(Request.Response.OK, Request.Range.SELF, average / clientSubmission.size()));
                                        }
                                    } else {
                                        this.serverCCHandler.onRequestReceived(request);
                                    }
                                    
                                } else if (Request.Topic.valueOf(this.request.getTopic()).equals(Request.Topic.COUNT)) {
    
                                    System.out.println("Response: COUNT");
                                    System.out.flush();
                                    if (Request.Range.valueOf(this.request.getRange()).equals(Request.Range.SELF)) {
                                        respondToClient(RequestFactory.serverCountResponse(Request.Response.OK, Request.Range.SELF, clientSubmission.size()));
                                    } else {
                                        this.serverCCHandler.onRequestReceived(request);
                                    }
                                    
                                } else if (Request.Topic.valueOf(this.request.getTopic()).equals(Request.Topic.HISTORY)) {
    
                                    System.out.println("Response: HISTORY");
                                    System.out.flush();
                                    if (Request.Topic.valueOf(this.request.getRange()).equals(Request.Range.SELF)) {
                                    
                                    } else {
                                        this.serverCCHandler.onRequestReceived(request);
                                    }
                                    
                                } else {
                                    System.out.println("Reached the else");
                                    this.serverCCHandler.onRequestReceived(request);
                                }
                                
                                //TODO More requests handling here
                                
                                // Theoretically, the ClientAdapter to validate the inputs to be only numbers
                                // Should there be any non-numbers, it would mean its commands to request informing or write
                                // So any other messages received by ClientConnection should be submitted as a request to add
                                // to the sum
                                
                            }
                        } catch (SocketTimeoutException e) {
                            // Restart read
                        } catch (EOFException e) {
                            //Reached end of file, do stuff
                            if (!this.clientSocket.isConnected()) {
                                //TODO Client terminated abruptly. Stop gracefully
                                break;
                            }
//                            e.printStackTrace();
                        }
                    }
                } catch (SocketException e) {
                    //TODO Handler
                    //ServerSocket was closed
                } catch (ClassNotFoundException e) {
                    //SHOULD NEVER HAPPEN
                    e.printStackTrace();
                } catch (IOException e) {
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
            try{
                Request disconnectClient = RequestFactory.serverDisconnect();
                this.out.writeObject(disconnectClient);
                this.out.flush();
                this.wait(1000);
            } catch (SocketException e){
                //TODO Handler
                //Already closed
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
    
    public void respondToClient( Request response ) {
        
        try {
            this.out.writeObject(response);
            this.out.flush();
        } catch (IOException e) {
            //TODO Handler ("Could not write requestToServer");
            e.printStackTrace();
        }
    }
    
    public interface ClientConnectionHandler {
        
        void onRequestReceived( Request request );
        void onRequestSubmissionFailure( String reason );
    }
}
