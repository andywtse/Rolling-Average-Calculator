package back.network;

import back.interfacing.ServerUI;

/**
 * This class is used to start a network connection for a {@link Client} to
 * link to. It will perform various actions and communicate back to the
 * {@link Client}.
 */
public class Server {

    /**
     * The interface that a user will use to communicate with this. It is
     * preferred to communicate back to this handler on a non-UI thread.
     */
    private ServerUI UIHandler;
    private boolean isShuttingDown = false;

    /**
     * Establish a link to the communication interface that the user
     * will use.
     * @param handler The communication interface being used.
     */
    public void setUIHandler(final ServerUI handler) {
        UIHandler = handler;
    }

    /**
     * Create the network server and start listening for {@link Client}s.
     *
     * @param ipAddress The IPv4 or IPv6 address to create the network server.
     * @param port The port number to open for the network server.
     */
    public void spinUp(final String ipAddress, final String port) {
        // FIXME 11/07/17: Fill this method in correctly.
        new Thread(()->{
            final boolean isSuccessful = Math.random() < 0.99f;
            if (UIHandler != null) {
                if (isSuccessful) {
                    UIHandler.onSpinUpSuccess();
                    for (int i = 0; i < 3; ++i) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        UIHandler.onClientConnected("127.0.0.1");
                    }
                } else {
                    UIHandler.onSpinUpFailure("You were unlucky.");
                }
            } else {
                System.err.println("Attempted to create server without "
                        + "user interface.");
                System.exit(-1);
            }
        }).start();
    }

    /**
     * Go through all {@link Client}'s and disconnect from them safely so
     * they can exit properly.
     */
    public synchronized void shutDown() {
        if (isShuttingDown) {
            return;
        }
        isShuttingDown = true;
        // FIXME 11/07/17: Fill this method in correctly.
        new Thread(()->{
            for (int i = 0; i < 3; ++i) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // Regardless of whether the UIHandler is null, we want
                // to shutdown all connections since we know we may exit.
                if (UIHandler != null) {
                    UIHandler.onClientDisconnected("127.0.0.1");
                }
            }
            if (UIHandler != null) {
                final boolean isSuccessful = Math.random() < 0.99f;
                if (isSuccessful) {
                    UIHandler.onShutdownSuccess();
                } else {
                    UIHandler.onShutdownFailure("You were unlucky.");
                }
            }
        }).start();
    }

    @Override
    public String toString() {
        // TODO 11/07/17: Remove this toString or make it meaningful.
        return this.getClass().getSimpleName();
    }
}
