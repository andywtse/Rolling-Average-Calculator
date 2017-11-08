package back.network;

import back.interfacing.ClientUI;

/**
 * This class is used to create a network connection to the {@link Server}
 * for a user to enter numbers.
 */
public class Client {

    /**
     * The interface that a user will use to communicate with this. It is
     * preferred to communicate back to this handler on a non-UI thread.
     */
    private ClientUI UIHandler;

    /**
     * Establish a link to the communication interface that the user
     * will use.
     * @param handler The communication interface being used.
     */
    public void setUIHandler(final ClientUI handler) {
        UIHandler = handler;
    }

    /**
     * Attempt to create a network connection to a {@link Server}. Whether this
     * succeeds or fails, this method should communicate back to the
     * {@link ClientUI}.
     *
     * @param ipAddress The IPv4 or IPv6 network address to connect to.
     * @param port The port of the server to connect to.
     */
    public void connect(final String ipAddress, final String port) {
        System.out.println("Attempting to connect to " + ipAddress
            + ":" + port);
        // FIXME 11/07/17: Fill in method and remove the random calls.
        if (UIHandler != null) {
            new Thread(() -> {
                // Simulate a network delay.
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                final boolean didConnect =  Math.random() > 0.99f;
                if (didConnect) {
                    UIHandler.onConnectionSuccess();
                } else {
                    UIHandler.onConnectionFailure(
                        "You were unlucky, I guess.");
                }
            }).start();
        } else {
            System.err.println("Attempted to connect without user interface.");
            System.exit(-1);
        }
    }

    /**
     * Close any existing connections and clean up variables so any connected
     * {@link Server} will not throw exceptions when this program ends.
     */
    public void disconnect() {
        // FIXME 11/07/17: Fill this method in correctly.
        System.out.println("Disconnected!");
    }

    @Override
    public String toString() {
        // TODO 11/07/17: Remove this toString or make it meaningful.
        return this.getClass().getSimpleName();
    }
}
