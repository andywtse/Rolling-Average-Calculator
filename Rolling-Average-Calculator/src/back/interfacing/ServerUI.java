package back.interfacing;

/**
 * The communication interface for a {@link back.network.Server}. Any means of
 * user input must implement this to use the {@link back.network.Server}.
 */
public interface ServerUI {

    /**
     * Callback to UI to inform user that the {@link back.network.Server}
     * started up successfully. The user can now take additional actions.
     */
    void onSpinUpSuccess();

    /**
     * Callback to UI to inform user that the {@link back.network.Server}
     * could not create a network server correctly. An example reason is that
     * the port intended for the server is already in use.
     *
     * @param reason The reason for the failure to create a server.
     */
    void onSpinUpFailure(final String reason);

    /**
     * Callback to UI to inform user that a {@link back.network.Client} has
     * connected to their {@link back.network.Server}.
     * @param ipAddress The newly connected {@link back.network.Client}'s
     *                  IPv4 or IPv6 address.
     */
    void onClientConnected(final String ipAddress);

    /**
     * Callback to UI to inform user that a {@link back.network.Client} has
     * disconnected from their {@link back.network.Server}.
     *
     * @param ipAddress The IPv4 or IPv6 address of the
     * {@link back.network.Client} that disconnected.
     */
    void onClientDisconnected(final String ipAddress);

    /**
     * Callback to UI to inform user that all {@link back.network.Client}s
     * have been disconnected as requested by the user.
     */
    void onShutdownSuccess();

    /**
     * Callback to UI to inform user that the {@link back.network.Server}
     * was unable to shutdown properly.
     *
     * @param reason The reason or insight on why the server failed to shutdown
     *               properly.
     */
    void onShutdownFailure(final String reason);

    /**
     * Callback to UI to inform user that their connection to the
     * {@link back.network.Client} has stopped. Example reasons of
     * failure include the {@link back.network.Client} shutting down
     * without warning.
     */
    void onConnectionBroken(final String reason);
}
