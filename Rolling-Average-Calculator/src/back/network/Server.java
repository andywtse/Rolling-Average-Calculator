package back.network;

import back.interfacing.ServerUI;

/**
 * This class is used to start a network connection for a {@link Client} to
 * link to. It will perform various actions and communicate back to the
 * {@link Client}.
 */
public class Server {

    // The interface that a user will use to communicate with this.
    private ServerUI UIHandler;

    /**
     * Establish a link to the communication interface that the user
     * will use.
     * @param handler The communication interface being used.
     */
    public void setUIHandler(final ServerUI handler) {
        UIHandler = handler;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
