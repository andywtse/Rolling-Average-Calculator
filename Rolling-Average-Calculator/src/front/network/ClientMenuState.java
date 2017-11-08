package front.network;

/**
 * The menu state of the user-facing client. This is used to determine
 * what messages to show to the user and what kind of interaction to
 * request from them.
 */
public enum ClientMenuState {
    RequestServerInfo,
    MainMenu,
    PromptReconnect,
    ConnectionFailed,
}
