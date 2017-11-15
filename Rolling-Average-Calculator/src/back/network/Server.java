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

    /**
     * The interface that a user will use to communicate with this. It is
     * preferred to communicate back to this handler on a non-UI thread.
     */
    private ServerUI UIHandler;
    private boolean isShuttingDown = false;
    private ServerPool server;
    private Thread threadServer;

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
    public void spinUp(final String ipAddress, final String port) {
        new Thread(()-> {
            server = new ServerPool(Integer.parseInt(port));
            threadServer = new Thread(server);
            threadServer.start();
            if (threadServer.isAlive()) {
                UIHandler.onSpinUpSuccess();
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
    public boolean terminate(){



        try {

            this.threadPool.shutdown();
            this.serverSocket.close();

            this.threadPool.awaitTermination(60000, TimeUnit.NANOSECONDS);
            System.out.println("ServerAdapter Stopped.") ;

            this.isStopped = true;

        } catch (IOException e) {
            //TODO Handler
//            UIHandler.onShutdownFailure("Error closing server");
            System.out.println("Error closing server");
        } catch (InterruptedException e) {
            //TODO Handler
//            UIHandler.onShutdownFailure("Server shutdown was interrupted");
            System.out.println("Server shutdown was interrupted");
        }
        isShuttingDown = true;

//        // FIXME 11/07/17: Fill this method in correctly.
//        new Thread(()->{
//            for (int i = 0; i < 3; ++i) {
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                // Regardless of whether the UIHandler is null, we want
//                // to shutdown all connections since we know we may exit.
//                if (UIHandler != null) {
//                    UIHandler.onClientDisconnected("127.0.0.1");
//                }
//            }
//            if (UIHandler != null) {
//                final boolean isSuccessful = Math.random() < 0.99f;
//                if (isSuccessful) {
//                    UIHandler.onShutdownSuccess();
//                } else {
//                    UIHandler.onShutdownFailure("You were unlucky.");
//                }
//            }
//        }).start();
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
            //TODO Handler
            System.out.println("Could not get host: "+serverAddress);
        }catch (IOException e) {
//            UIHandler.onSpinUpFailure("Could not open server port");
            //TODO Handler
            System.out.println("Could not open server port "+serverPort);
        }

    }
}