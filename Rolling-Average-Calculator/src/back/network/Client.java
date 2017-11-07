package back.network;

import back.interfacing.ClientUI;

/**
 * This class is used to create a network connection to the {@link Server}
 * for a user to enter numbers.
 */
public class Client {

    // The interface that a user will use to communicate with this.
    private ClientUI UIHandler;

    /**
     * Establish a link to the communication interface that the user
     * will use.
     * @param handler The communication interface being used.
     */
    public void setUIHandler(final ClientUI handler) {
        UIHandler = handler;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
