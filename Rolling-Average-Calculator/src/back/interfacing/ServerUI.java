package back.interfacing;

import back.network.client.ClientAdapter;
import back.network.server.ServerAdapter;

/**
 * The communication interface for a {@link ServerAdapter}. Any means of
 * user input must implement this to use the {@link ServerAdapter}.
 */
public interface ServerUI {
    
    /**
     * Callback to UI to inform user that the {@link ServerAdapter}
     * started up successfully. The user can now take additional actions.
     */
    void onSpinUpSuccess();
    
    /**
     * Callback to UI to inform user that the {@link ServerAdapter}
     * could not create a network server correctly. An example reason is that
     * the port intended for the server is already in use.
     *
     * @param reason The reason for the failure to create a server.
     */
    void onSpinUpFailure( final String reason );
    
    /**
     * Callback to UI to inform user that a {@link ClientAdapter} has
     * connected to their {@link ServerAdapter}.
     *
     * @param ipAddress The newly connected {@link ClientAdapter}'s
     *                  IPv4 or IPv6 address.
     */
    void onClientConnected( final String ipAddress );
    
    /**
     * Callback to UI to inform user that a {@link ClientAdapter} has
     * disconnected from their {@link ServerAdapter}.
     *
     * @param ipAddress The IPv4 or IPv6 address of the
     *                  {@link ClientAdapter} that disconnected.
     */
    void onClientDisconnected( final String ipAddress );
    
    /**
     * Callback to UI to inform user that all {@link ClientAdapter}s
     * have been disconnected as requested by the user.
     */
    void onShutdownSuccess();
    
    /**
     * Callback to UI to inform user that the {@link ServerAdapter}
     * was unable to shutdown properly.
     *
     * @param reason The reason or insight on why the server failed to shutdown
     *               properly.
     */
    void onShutdownFailure( final String reason );
    
    /**
     * Callback to UI to inform user that their connection to the
     * {@link ClientAdapter} has stopped. Example reasons of
     * failure include the {@link ClientAdapter} shutting down
     * without warning.
     */
    void onConnectionBroken( final String reason );
}
