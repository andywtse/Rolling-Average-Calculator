package back.network.server;

import utility.request.Request;
import utility.request.RequestFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
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
    private int clientSum = 0;
    
    
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
            
            clientConnectionThread = new Thread(this::requestFromServer);
            clientConnectionThread.start();
            
        } catch (InterruptedException e) {
            // We've been interrupted: no more messages.
            return;
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void requestFromServer() {
        
        String jsonInput;
        
        while (!isStopped) {
            try {
                jsonInput = (String) this.in.readObject();
                if (jsonInput != null) {
                    System.out.println(jsonInput);
                    System.out.flush();
                    Request request = new Request.Builder().fromJSONString(jsonInput).build();
                    
                    switch (request.getTopic()) {
                        case SUBMIT:
                            processSubmit(request);
                            break;
                        case AVERAGE:
                            processAverage(request);
                            break;
                        case COUNT:
                            processCount(request);
                            break;
                        case HISTORY:
                            processHistory(request);
                            break;
                        case USERS:
                            serverCCHandler.onRequestReceived(request);
                            break;
                        case DISCONNECT:
                            serverCCHandler.onRequestReceived(request);
                            break;
                    }
                }
            } catch (EOFException e) {
                //TODO Figure out how to handle this
            } catch (ClassNotFoundException e) {
                //TODO Figure out how to handle this
            } catch (IOException e) {
                //TODO Handle this exception
            }
        }
    }
    
    private void processSubmit( Request request ) {
        
        clientSubmission.add(request.getAmount());
        clientSum += request.getAmount();
        
        serverCCHandler.onRequestReceived(request);
    }
    
    private void processAverage( Request request ) {
        
        switch (request.getRange()) {
            case ALL:
                serverCCHandler.onRequestReceived(request);
                break;
            case SELF:
                int average = 0;
                if (clientSubmission.size() > 0) {
                    average = clientSum / clientSubmission.size();
                }
                Request response = RequestFactory.serverAverageResponse(Request.Response.OK, Request.Range.SELF, average);
                respondToClient(response);
                break;
        }
    }
    
    private void processCount( Request request ) {
        
        switch (request.getRange()) {
            case ALL:
                serverCCHandler.onRequestReceived(request);
                break;
            case SELF:
                Request response = RequestFactory.serverCountResponse(Request.Response.OK, Request.Range.SELF, clientSubmission.size());
                respondToClient(response);
                break;
        }
    }
    
    private void processHistory( Request request ) {
        
        switch (request.getRange()) {
            case ALL:
                serverCCHandler.onRequestReceived(request);
                break;
            case SELF:
                Request response = RequestFactory.serverHistoryResponse(Request.Response.OK, Request.Range.SELF, clientSubmission);
                respondToClient(response);
                break;
        }
    }
    
    /**
     * Writes the response {@link Request} to outputstream in UTF to send to the client
     *
     * @param response A response that is made by the server to appease a request
     */
    public void respondToClient( Request response ) {
        
        try {
            this.out.writeObject(response.toJSONString());
            this.out.flush();
        } catch (IOException e) {
            //TODO Handler ("Could not write requestToServer");
            e.printStackTrace();
        }
    }
    
    /**
     * Attempts to send a disconnect from server response, only when the socket connection is open.
     * Otherwise, interrupt the thread and close the inputs and outputs and the socket.
     *
     * @return Validation if termination was success
     */
    public synchronized boolean terminateConnection() {
        
        try {
            try {
                Request disconnectClient = RequestFactory.serverDisconnect();
                this.out.writeObject(disconnectClient.toJSONString());
                this.out.flush();
                this.wait(500);
            } catch (SocketException e) {
                //TODO Handler
                //Already closed
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            this.clientConnectionThread.interrupt();
            try {
                this.wait(2000);
            } catch (InterruptedException e) {
                //TODO Make handler for this
                //Wait for interrupt
            }
            
            this.in.close();
            this.out.close();
            
            this.isStopped = true;
            
            this.clientSocket.close();
            
            return this.isStopped;
        } catch (IOException e) {
            //TODO Handler "Error on closing client connection");
            e.printStackTrace();
        }
        
        return this.isStopped;
    }
    
    /**
     * The communication interface for the ClientConnection to the {@link Server}.
     */
    public interface ClientConnectionHandler {
        
        /**
         * Callback to the {@link Server} to inform that the client has made a request.
         * The server must deal with the request and send a response back
         */
        void onRequestReceived( Request request );
    }
}
