package back.network.client;

import back.interfacing.ClientUI;
import back.network.server.ServerAdapter;
import utility.request.Request;
import utility.request.RequestFactory;

import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is used to create a network connection to the {@link back.network.server.Server}
 * for a user to enter numbers.
 */
public class ClientAdapter implements Client.ClientHandler {
    
    public static final int WAIT_DELAY_MS = 1000;
    
    private ClientUI UIHandler;
    private Client client;
    private Thread threadClient;
    private boolean isShuttingDown = false;
    private ReentrantLock stateLock;
    private long clientID;
    
    /**
     * Establish a link to the communication interface that the user
     * will use.
     *
     * @param handler The communication interface being used.
     */
    public void setUIHandler( final ClientUI handler ) {
        
        UIHandler = handler;
    }
    
    /**
     * Attempts to create a new {@link Client} that will run on a separate thread to maintain
     * its own connection with the server. Whether the thread starts of fail, it will communicate
     * back to the {@link ClientUI}. Must be done in a non-UI thread.
     *
     * @param ipAddress The IPv4 or IPv6 network address to connect to.
     * @param port      The port of the server to connect to.
     */
    public void connect( final String ipAddress, final String port ) {
        
        System.out.println("Attempting to connect to " + ipAddress + ":" + port);
        
        new Thread(() -> {
            stateLock = new ReentrantLock();
            this.client = new Client(ipAddress, Integer.parseInt(port));
            this.client.setCCHandler(this);
            threadClient = new Thread(this.client);
            threadClient.start();
            if (threadClient.isAlive()) {
                this.UIHandler.onConnectionSuccess();
            } else {
                this.UIHandler.onConnectionFailure("Could not start Client thread");
            }
            
        }).start();
        
    }
    
    /**
     * Close any existing connections and clean up variables so any connected
     * {@link ServerAdapter} will not throw exceptions when this program ends.
     */
    public synchronized void disconnect() {
        
        if (isShuttingDown) {
            return;
        }
        isShuttingDown = true;
        
        if (client.shutdown()) {
            if (threadClient != null) {
                threadClient.interrupt();
                try {
                    this.wait(WAIT_DELAY_MS);
                    //TODO Handler needs to inform ClientLauncher that disconnect was success
                } catch (InterruptedException e) {
                    UIHandler.onConnectionBroken("Closure of client thread was interrupted");
                }
                if (threadClient.isAlive()) {
                    UIHandler.onConnectionBroken("User terminated connection");
                } else {
                    UIHandler.onConnectionBroken("Failed to terminate connection");
                }
            }
        } else {
            UIHandler.onConnectionFailure("Socket could not close");
        }
    }
    
    /**
     * Creates a specific {@link Request} from the given parameters, Topic and Range. This will then
     * pass it to the {@link Client} to process the Request.
     *
     * @param topic The type of request is being made from the user
     * @param range Determines if request applies to ALL or SELF
     */
    public void sendRequest( Request.Topic topic, Request.Range range ) {
        
        Request request = null;
        
        switch (topic) {
            case AVERAGE:
                request = RequestFactory.clientAverageRequest(clientID, range);
                break;
            case COUNT:
                request = RequestFactory.clientCountRequest(clientID, range);
                break;
            case HISTORY:
                request = RequestFactory.clientHistoryRequest(clientID, range);
                break;
            case USERS:
                request = RequestFactory.clientUsersRequest(clientID);
                break;
            case DISCONNECT:
                request = RequestFactory.clientDisconnect(clientID);
                break;
            default:
                //Should never get here. However, in the case it does, terminate the client
                request = RequestFactory.clientDisconnect(clientID);
                //TODO Make suitable UIHandler callback to handle this .onRequestNotMatching()
                disconnect();
                break;
        }
        
        this.client.requestToServer(request);
    }
    
    /**
     * Creates a submit request {@link Request} from the value and passes it to the {@link Client}
     * to process the information
     *
     * @param value a value the client submits to the server
     */
    public void sendValue( int value ) {
        
        Request request = RequestFactory.clientSubmitRequest(clientID, value);
        this.client.requestToServer(request);
        
    }
    
    @Override
    public void onOpenSocketSuccess() {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String success = "CCHandler: Socket successfully opened";
        System.out.println(success);
        stateLock.unlock();
    }
    
    @Override
    public void onOpenSocketFailure( String reason ) {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String failure = "CCHandler: Socket failed to opened due to: " + reason;
        System.out.println(failure);
        
        //TODO Terminate client thread immediately
        
        stateLock.unlock();
    }
    
    @Override
    public void onServerConnected( String ipAddress ) {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String connection = "CCHandler: Server IP Address: " + ipAddress + " disconnected";
        System.out.println(connection);
        stateLock.unlock();
    }
    
    @Override
    public void onConnectionBroken( String reason ) {
        //TODO
    }
    
    @Override
    public void onShutdownSuccess() {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String success = "CCHandler: Client Thread successfully shutdown";
        System.out.println(success);
        stateLock.unlock();
    }
    
    @Override
    public void onShutdownFailure( String reason ) {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String failure = "CCHandler: Client Thread failed to shutdown due to: " + reason;
        System.out.println(failure);
        stateLock.unlock();
    }
    
    
    @Override
    public void onIOSocketFailure( final String reason ) {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String failure = "CCHandler: IO broken due to: " + reason;
        System.out.println(failure);
        stateLock.unlock();
    }
    
    @Override
    public void onClientIdObtained( final long id ) {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String identity = "Client ID: " + this.clientID;
        System.out.println(identity);
        stateLock.unlock();
    }
    
    @Override
    public void onRequestFailure( String reason ) {
        //TODO
    }
}
