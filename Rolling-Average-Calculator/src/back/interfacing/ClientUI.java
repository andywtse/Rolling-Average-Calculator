package back.interfacing;

/**
 * The communication interface for a {@link back.network.Client}. Any means of
 * user input must implement this to use the {@link back.network.Client}.
 */
public interface ClientUI {
    /**
     * Callback to UI to inform user that they connected properly.
     * They should then get new engagement options.
     */
    void onConnectionSuccess();

    /**
     * Callback to UI to inform user that they could not connect to
     * the {@link back.network.Server}. Example reasons of failure
     * include not finding the {@link back.network.Server} or it
     * is not accepting any more connections.
     *
     * @param reason The reason for the failed connection.
     */
    void onConnectionFailure(final String reason);

    /**
     * Callback to UI to inform user that their connection to the
     * {@link back.network.Server} has stopped. Example reasons of
     * failure include the {@link back.network.Server} shutting down
     * without warning.
     */
    void onConnectionBroken(final String reason);
}
