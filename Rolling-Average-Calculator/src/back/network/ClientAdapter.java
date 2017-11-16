package back.network;

import back.interfacing.ClientUI;

/**
 * This class is used to create a network connection to the {@link ServerAdapter}
 * for a user to enter numbers.
 */
public class ClientAdapter {

    /**
     * The interface that a user will use to communicate with this. It is
     * preferred to communicate back to this handler on a non-UI thread.
     */
    private ClientUI UIHandler;
    private Client client;
    private Thread threadClient;
    private boolean isShuttingDown =false;

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

        client.terminate();
    }

    /**
     * Sends a message to the server from the client
     *
     * @param msg The String message sent by the client
     */
    public void sendMessage(String msg){
        this.client.messageToServer(msg);

    }
}
