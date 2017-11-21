package back.network.client;

import back.interfacing.ClientUI;
import back.network.server.ServerAdapter;

import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is used to create a network connection to the {@link ServerAdapter}
 * for a user to enter numbers.
 */
public class ClientAdapter implements Client.ClientHandler {
    
    public static final int WAIT_DELAY_MS = 1000;
    
    /**
     * The interface that a user will use to communicate with this. It is
     * preferred to communicate back to this handler on a non-UI thread.
     */
    private ClientUI UIHandler;
    private Client client;
    private Thread threadClient;
    private boolean isShuttingDown = false;
    private ReentrantLock stateLock;
    
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
     * Attempt to create a network connection to a {@link ServerAdapter}. Whether this
     * succeeds or fails, this method should communicate back to the
     * {@link ClientUI}.
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
        
        //TODO Send disconnect to server
        
        if (isShuttingDown) {
            return;
        }
        isShuttingDown = true;
        
        if (client.terminate()) {
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
     * Sends a message to the server from the client
     *
     * @param command The command message sent by the client
     */
    public void sendCommand( String command, String range ) {
        //TODO Check if commands are valid or if the message is purely numbers
        this.client.commandToServer(command, range);
        
    }
    
    /**
     * Sends a value to the server from the client
     *
     * @param value The value sent by the client
     */
    public void sendValue( int value ) {
        
        this.client.valueToServer(value);
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
    public void onClientDisconnected( String ipAddress, long clientID ) {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String connection = "CCHandler: ClientID: " + clientID + " - Client IP Address: " + ipAddress + " disconnected";
        System.out.println(connection);
        stateLock.unlock();
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
    public void onConnectionBroken( String reason ) {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String failure = "CCHandler: Client Connection broken due to: " + reason;
        UIHandler.onConnectionBroken(failure);
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
}
