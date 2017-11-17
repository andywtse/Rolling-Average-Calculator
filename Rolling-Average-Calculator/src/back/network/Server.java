package back.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable{

    private ServerHandler SSHandler;

    private int serverPort;
    private String serverAddress;

    private ServerSocket serverSocket = null;
    private ExecutorService threadPool = Executors.newFixedThreadPool(4);

    private boolean isStopped = false;
    private Map<Integer,InetAddress> clientIdConnection = new HashMap<Integer,InetAddress>();
    private AtomicInteger clientId = new AtomicInteger(0);

    /**
     * Handler to communicate with ServerAdapter
     */
    public interface ServerHandler{

        void onOpenSocketSuccess();
        void onOpenSocketFailure(final String reason);
        void onClientConnected(final String ipAddress, final int clientID);
        void onClientDisconnected(final String ipAddress, final int clientID);
        void onShutdownSuccess();
        void onShutdownFailure(final String reason);
        void onConnectionBroken(final String reason);

    }

    /**
     * Creates new Server with address and port
     *
     * @param address The IPv4 or IPv6 address to create the network server.
     * @param port The port number to open for the network server.
     */
    public Server( String address, int port ){
        this.serverPort = port;
        this.serverAddress = address;
    }

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

        while(!isStopped){
            Socket clientSocket=null;
            try{
                clientSocket = this.serverSocket.accept();
            }catch (IOException e){
                if(isStopped){
                    System.out.println("ServerAdapter has stopped");
                    break;
                }
                SSHandler.onOpenSocketFailure("Could not open clientSocket");
                break;
            }
            System.out.println("New client: "+clientSocket.getLocalAddress());
            int curClientID=clientId.getAndIncrement();
            clientIdConnection.put(curClientID,clientSocket.getLocalAddress());

            SSHandler.onClientConnected(clientSocket.getLocalAddress().toString(),curClientID);

            this.threadPool.execute(new ServerThread(clientSocket,curClientID));
        }

    }

    /**
     * Shutdown the Server by closing the thread pool and closing
     * the sockets of Client and Servers
     */
    public boolean terminate(){

        try {
            this.threadPool.shutdown();
            this.serverSocket.close();

            this.threadPool.awaitTermination(60000, TimeUnit.NANOSECONDS);
            System.out.println("ServerAdapter Stopped.") ;

            this.isStopped = true;
            SSHandler.onShutdownSuccess();

        } catch (IOException e) {
            SSHandler.onShutdownFailure("IOException: Error closing server");
        } catch (InterruptedException e) {
            SSHandler.onShutdownFailure("Server shutdown was interrupted ");
        }

        //TODO Shutdown each clientSocket gracefully by alerting each client

        return this.isStopped;
    }

    /**
     * Opens the server socket with designated port and address
     */
    private void openServerSocket() {

        try {

            InetAddress address = InetAddress.getByName(this.serverAddress);
            this.serverSocket = new ServerSocket(this.serverPort,50,address);

        }catch (UnknownHostException e){
            SSHandler.onOpenSocketFailure("Could not get host: "+serverAddress);
        }catch (IOException e) {
            SSHandler.onOpenSocketFailure("Could not open server port "+serverPort);
        }

    }
}
