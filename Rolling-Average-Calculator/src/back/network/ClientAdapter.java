package back.network;

import back.interfacing.ClientUI;

import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is used to create a network connection to the {@link ServerAdapter}
 * for a user to enter numbers.
 */
public class ClientAdapter implements Client.ClientHandler{

    /**
     * The interface that a user will use to communicate with this. It is
     * preferred to communicate back to this handler on a non-UI thread.
     */
    private ClientUI UIHandler;
    private Client client;
    private Thread threadClient;
    private boolean isShuttingDown =false;

    private ReentrantLock stateLock;

    /**
     * Establish a link to the communication interface that the user
     * will use.
     *
     * @param handler The communication interface being used.
     */
    public void setUIHandler(final ClientUI handler) {
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
    public void connect(final String ipAddress, final String port) {
        System.out.println("Attempting to connect to " + ipAddress + ":" + port);

        new Thread(() -> {

            this.client = new Client(ipAddress,Integer.parseInt(port));
            this.client.setCCHandler(this);
            threadClient = new Thread(this.client);
            threadClient.start();
            if(threadClient.isAlive()) {
                this.UIHandler.onConnectionSuccess();
            }else {
                this.UIHandler.onConnectionFailure("Could not start Client thread");
            }

        }).start();

    }

    /**
     * Close any existing connections and clean up variables so any connected
     * {@link ServerAdapter} will not throw exceptions when this program ends.
     */
    public void disconnect() {


        if (isShuttingDown) {
            return;
        }
        isShuttingDown = true;

        if(client.terminate()){
            UIHandler.onConnectionBroken("User terminated connection");
            //TODO Terminate client itself
        }else{
            UIHandler.onConnectionFailure("Socket could not close");
        }
    }

    /**
     * Sends a message to the server from the client
     *
     * @param msg The String message sent by the client
     */
    public void sendMessage(String msg){
        this.client.messageToServer(msg);

    }

    @Override
    public void onOpenSocketSuccess() {
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String success = "Socket successfully opened";
        System.out.println(success);
        stateLock.unlock();
    }

    @Override
    public void onOpenSocketFailure(String reason) {
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String failure = "Socket failed to opened due to: " + reason;
        System.out.println(failure);

        //TODO Terminate client thread immediately

        stateLock.unlock();
    }

    @Override
    public void onServerConnected(String ipAddress) {
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String connection = "Server IP Address: " +ipAddress+ " disconnected";
        System.out.println(connection);
        stateLock.unlock();
    }

    @Override
    public void onClientDisconnected(String ipAddress,int clientID) {
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String connection = "ClientID: " + clientID + " - Client IP Address: " +ipAddress+ " disconnected";
        System.out.println(connection);
        stateLock.unlock();
    }

    @Override
    public void onShutdownSuccess() {
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String success = "Client Thread successfully shutdown";
        System.out.println(success);
        stateLock.unlock();
    }

    @Override
    public void onShutdownFailure(String reason) {
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String failure = "Client Thread failed to shutdown due to: " + reason;
        System.out.println(failure);
        stateLock.unlock();
    }

    @Override
    public void onConnectionBroken(String reason) {
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String failure = "Client Connection broken due to: " + reason;
        System.out.println(failure);
        stateLock.unlock();
    }

    @Override
    public void onIOSocketFailure(final String reason){
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String failure = "IO broken due to: " + reason;
        System.out.println(failure);
        stateLock.unlock();
    }
}
