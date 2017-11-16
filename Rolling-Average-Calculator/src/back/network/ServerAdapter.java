package back.network;

import back.interfacing.ServerUI;

/**
 * This class is used to start a network connection for a {@link ClientAdapter} to
 * link to. It will perform various actions and communicate back to the
 * {@link ClientAdapter}.
 */
public class ServerAdapter {

    /**
     * The interface that a user will use to communicate with this. It is
     * preferred to communicate back to this handler on a non-UI thread.
     */
    private ServerUI UIHandler;
    private boolean isShuttingDown = false;
    private Server server;
    private Thread threadServer;

    /**
     * Establish a link to the communication interface that the user
     * will use.
     * @param handler The communication interface being used.
     */
    public void setUIHandler(final ServerUI handler) {
        UIHandler = handler;
    }

    /**
     * Create the network server and start listening for {@link ClientAdapter}s.
     *
     * @param ipAddress The IPv4 or IPv6 address to create the network server.
     * @param port The port number to open for the network server.
     */
    public void spinUp(final String ipAddress, final String port) {
        new Thread(()-> {



            server = new Server(ipAddress,Integer.parseInt(port));
            threadServer = new Thread(server);
            threadServer.start();
            if (threadServer.isAlive()) {
                UIHandler.onSpinUpSuccess();
            }else{
                UIHandler.onSpinUpFailure("ServerAdapter thread did not start");
            }
        }).start();
    }

    /**
     * Go through all {@link ClientAdapter}'s and disconnect from them safely so
     * they can exit properly.
     */
    public void shutDown() {
        if (isShuttingDown) {
            return;
        }
        isShuttingDown = true;

        if(server.terminate()){
            UIHandler.onShutdownSuccess();
        }else{
            UIHandler.onShutdownFailure("Could not not close all connections regarding server");
        }
    }

}