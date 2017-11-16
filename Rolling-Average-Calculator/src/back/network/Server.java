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

    private int serverPort;
    private String serverAddress;

    private ServerSocket serverSocket = null;
    private ExecutorService threadPool = Executors.newFixedThreadPool(4);

    private boolean isStopped = false;
    private Map<Integer,InetAddress> clientIdConnection = new HashMap<Integer,InetAddress>();
    private AtomicInteger clientId = new AtomicInteger(0);


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


    /**
     * Creates new thread for every new connection from clients
     */
    @Override
    public void run() {

        openServerSocket();

        while(!isStopped){
            Socket clientSocket;
            try{
                clientSocket = this.serverSocket.accept();
            }catch (IOException e){
                if(isStopped){
                    System.out.println("ServerAdapter has stopped");
                    break;
                }
                throw new RuntimeException("Error accepting client connection", e);
            }
            System.out.println("New client: "+clientSocket.getLocalAddress());
            int curClientID=clientId.getAndIncrement();
            clientIdConnection.put(curClientID,clientSocket.getLocalAddress());
            this.threadPool.execute(new ServerThread(clientSocket,curClientID));
        }

    }

    /**
     * Shutdown the Server by closing the thread pool and closing
     * the sockets of Client and Servers
     */
    public void terminate(){

        this.isStopped = true;

        try {
            this.threadPool.shutdown();
            this.serverSocket.close();

            this.threadPool.awaitTermination(60000, TimeUnit.NANOSECONDS);
            System.out.println("ServerAdapter Stopped.") ;

        } catch (IOException e) {
            //TODO
//            UIHandler.onShutdownFailure("Error closing server");
            System.out.println("Error closing server");
        } catch (InterruptedException e) {
            //TODO
//            UIHandler.onShutdownFailure("Server shutdown was interrupted");
            System.out.println("Server shutdown was interrupted");
        }

        //TODO Shutdown each clientSocket gracefully by alerting each client
    }

    /**
     * Opens the server socket with designated port and address
     */
    private void openServerSocket() {

        try {

            InetAddress address = InetAddress.getByName(this.serverAddress);
            this.serverSocket = new ServerSocket(this.serverPort,50,address);

        }catch (UnknownHostException e){
//            UIHandler.onSpinUpFailure("Could not get host " + serverAddress);
            //TODO
            System.out.println("Could not get host: "+serverAddress);
        }catch (IOException e) {
//            UIHandler.onSpinUpFailure("Could not open server port");
            //TODO
            System.out.println("Could not open server port "+serverPort);
        }

    }
}
