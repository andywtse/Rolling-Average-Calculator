package back.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable {

    public static final int TO_CLIENT = 1;
    public static final int TO_SERVER = 2;
    private ServerHandler SSHandler;
    private int serverPort;
    private String serverAddress;
    private ServerSocket serverSocket = null;
    private ExecutorService threadPool = Executors.newFixedThreadPool(4);
    private volatile boolean isStopped = false;
    private Map<Integer, InetAddress> clientIdConnection = new HashMap<Integer, InetAddress>();
    private AtomicInteger clientId = new AtomicInteger(0);
    private List<Future> listOfFutureServerThread = new ArrayList<>();
    private List<ServerThread> listOfServerThread = new ArrayList<>();

    /**
     * Creates new Server with address and port
     *
     * @param address The IPv4 or IPv6 address to create the network server.
     * @param port    The port number to open for the network server.
     */
    public Server(String address, int port) {
        this.serverPort = port;
        this.serverAddress = address;
    }

    /**
     * Establish a link to the communication interface that the user
     * will use.
     *
     * @param handler The communication interface being used.
     */
    public void setSSHandler(final ServerHandler handler) {
        SSHandler = handler;
    }

    /**
     * Creates new thread for every new connection from clients
     */
    @Override
    public void run() {

        openServerSocket();
        SSHandler.onOpenSocketSuccess();
        ServerThread serverThread;

        while (!isStopped) {
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if (isStopped) {
                    System.out.println("ServerAdapter has stopped");
                    break;
                }
                SSHandler.onOpenSocketFailure("Could not open clientSocket");
                this.isStopped = true;
                break;
            }

            // Puts the ID and Address into a map
            int curClientID = clientId.getAndIncrement();
            clientIdConnection.put(curClientID, clientSocket.getLocalAddress());

            // Creates the thread and puts it into a list of server threads
            // After, it saves reference a Future of the thread so it can be distinct within the threadPool since we do not own it
            SSHandler.onClientConnected(clientSocket.getLocalAddress().toString(), curClientID);
            serverThread = new ServerThread(clientSocket, curClientID);
            listOfServerThread.add(serverThread);
            Future futureThread = this.threadPool.submit(serverThread);
            listOfFutureServerThread.add(futureThread);
        }

    }

    /**
     * Shutdown the Server by closing the thread pool and closing
     * the sockets of Client and Servers
     */
    public boolean terminate() {

        //Shutdown each clientSocket gracefully by alerting each client
        for (ServerThread curServerThread : listOfServerThread) {
            curServerThread.terminateConnection(TO_CLIENT);
        }

        //Interrupts the Future thread in the list
        for (Future curFutureThread : listOfFutureServerThread) {
            curFutureThread.cancel(true);
        }


        //Tries to close:
        // Server socket
        // ThreadPool
        try {
            this.isStopped = true;
            this.serverSocket.close();

            this.threadPool.shutdown();

            this.threadPool.awaitTermination(60000, TimeUnit.NANOSECONDS);
            if (threadPool.isTerminated()) {
                SSHandler.onShutdownSuccess();
            } else {
                SSHandler.onShutdownFailure("Thread pool failed to terminate");
                isStopped = false;
            }

        } catch (IOException e) {
            SSHandler.onShutdownFailure("IOException: Error closing server");
        } catch (InterruptedException e) {
            SSHandler.onShutdownFailure("Server shutdown was interrupted ");
        }


        return this.isStopped;
    }

    /**
     * Opens the server socket with designated port and address
     */
    private void openServerSocket() {

        try {

            InetAddress address = InetAddress.getByName(this.serverAddress);
            this.serverSocket = new ServerSocket(this.serverPort, 50, address);

        } catch (UnknownHostException e) {
            SSHandler.onOpenSocketFailure("Could not get host: " + serverAddress);
        } catch (IOException e) {
            SSHandler.onOpenSocketFailure("Could not open server port " + serverPort);
        }

    }

    /**
     * Takes the response from Server Thread Handler and interrupts the thread that holds ClientID
     *
     * @param clientID Integer value unique to the client
     */
    private void terminateThread(int clientID) {
        //TODO Take response from handler and close thread with clientID
    }

    /**
     * Handler to communicate with ServerAdapter
     */
    public interface ServerHandler {

        void onOpenSocketSuccess();

        void onOpenSocketFailure(final String reason);

        void onClientConnected(final String ipAddress, final int clientID);

        void onClientDisconnected(final String ipAddress, final int clientID);

        void onShutdownSuccess();

        void onShutdownFailure(final String reason);

        void onConnectionBroken(final String reason);

    }
}
