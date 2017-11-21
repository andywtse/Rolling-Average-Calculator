package back.network.server;

import back.interfacing.ServerUI;
import back.network.client.ClientAdapter;

import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is used to start a network connection for a {@link ClientAdapter} to
 * link to. It will perform various actions and communicate back to the
 * {@link ClientAdapter}.
 */
public class ServerAdapter implements Server.ServerHandler {
    
    /**
     * The interface that a user will use to communicate with this. It is
     * preferred to communicate back to this handler on a non-UI thread.
     */
    private static final int WAIT_DELAY_MS = 1000;
    
    private ServerUI UIHandler;
    private boolean isShuttingDown = false;
    private Server server;
    private Thread threadServer;
    
    private ReentrantLock stateLock;
    
    /**
     * Establish a link to the communication interface that the user
     * will use.
     *
     * @param handler The communication interface being used.
     */
    public void setUIHandler( final ServerUI handler ) {
        
        UIHandler = handler;
    }
    
    /**
     * Create the network server and start listening for {@link ClientAdapter}s.
     *
     * @param ipAddress The IPv4 or IPv6 address to create the network server.
     * @param port      The port number to open for the network server.
     */
    public void spinUp( final String ipAddress, final String port ) {
        
        new Thread(() -> {
            stateLock = new ReentrantLock();
            server = new Server(ipAddress, Integer.parseInt(port));
            server.setSSHandler(this);
            threadServer = new Thread(server);
            threadServer.start();
            if (threadServer.isAlive()) {
                UIHandler.onSpinUpSuccess();
            } else {
                UIHandler.onSpinUpFailure("ServerAdapter thread did not start");
            }
        }).start();
    }
    
    /**
     * Go through all {@link ClientAdapter}'s and disconnect from them safely so
     * they can exit properly.
     */
    public synchronized void shutDown() {
        
        if (isShuttingDown) {
            return;
        }
        isShuttingDown = true;
        
        //Calls server's terminate which will terminate sockets and threads
        if (server.terminate()) {
            if (threadServer != null) {
                // if the thread exist, it will terminate it by throwing an Interrupt
                threadServer.interrupt();
                try {
                    this.wait(WAIT_DELAY_MS);
                } catch (InterruptedException e) {
                    System.out.println("ServerAdapter was interrupted during server shutdown");
                }
                if (!threadServer.isAlive()) {
                    UIHandler.onShutdownSuccess();
                } else {
                    UIHandler.onShutdownFailure("Server Thread is still alive");
                }
            }
        } else {
            UIHandler.onShutdownFailure("Could not not close all connections regarding server");
        }
    }
    
    @Override
    public void onOpenSocketSuccess() {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String success = "SSHandler: Server Socket successfully opened";
        System.out.println(success);
        stateLock.unlock();
    }
    
    @Override
    public void onOpenSocketFailure( String reason ) {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String failure = "SSHandler: Server Socket failed to opened due to: " + reason;
        System.out.println(failure);
        
        if (threadServer != null) {
            threadServer.interrupt();
            try {
                this.wait(WAIT_DELAY_MS);
                if (threadServer.isAlive()) {
                    UIHandler.onShutdownSuccess();
                }
            } catch (InterruptedException e) {
                System.out.println("ServerAdapter was interrupted during server termination");
            }
        }
        
        stateLock.unlock();
    }
    
    @Override
    public void onClientConnected( String ipAddress, long clientID ) {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String connection = "SSHandler: ClientID: " + clientID + " - Client IP Address: " + ipAddress + " connected";
        System.out.println(connection);
        stateLock.unlock();
    }
    
    @Override
    public void onClientDisconnected( String ipAddress, long clientID ) {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String connection = "SSHandler: ClientID: " + clientID + " - Client IP Address: " + ipAddress + " disconnected";
        System.out.println(connection);
        stateLock.unlock();
    }
    
    @Override
    public void onShutdownSuccess() {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String success = "SSHandler: Server Thread successfully shutdown";
        System.out.println(success);
        stateLock.unlock();
    }
    
    @Override
    public void onShutdownFailure( String reason ) {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String failure = "SSHandler: Server Thread failed to shutdown due to: " + reason;
        System.out.println(failure);
        stateLock.unlock();
    }
    
    @Override
    public void onConnectionBroken( String reason ) {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String failure = "SSHandler: Server Connection broken due to: " + reason;
        System.out.println(failure);
        stateLock.unlock();
    }
}