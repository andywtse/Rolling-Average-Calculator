package back.interfacing;

import back.network.ClientAdapter;
import back.network.ServerAdapter;

/**
 * The communication interface for a {@link ClientAdapter}. Any means of
 * user input must implement this to use the {@link ClientAdapter}.
 */
public interface ClientUI {
    /**
     * Callback to UI to inform user that they connected properly.
     * They should then get new engagement options.
     */
    void onConnectionSuccess();

    /**
     * Callback to UI to inform user that they could not connect to
     * the {@link ServerAdapter}. Example reasons of failure
     * include not finding the {@link ServerAdapter} or it
     * is not accepting any more connections.
     *
     * @param reason The reason for the failed connection.
     */
    void onConnectionFailure(final String reason);

    /**
     * Callback to UI to inform user that their connection to the
     * {@link ServerAdapter} has stopped. Example reasons of
     * failure include the {@link ServerAdapter} shutting down
     * without warning.
     */
    void onConnectionBroken(final String reason);
}
