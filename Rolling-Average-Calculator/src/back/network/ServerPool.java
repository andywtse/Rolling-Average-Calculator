package back.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServerPool implements Runnable{

    private int serverPort = 8080;

    private ServerSocket serverSocket = null;
    private Thread runningThread = null;
    private ExecutorService threadPool = Executors.newFixedThreadPool(20);

    private boolean isStopped = false;

    public ServerPool(int port){
        this.serverPort = port;
    }


    /**
     * Creates new thread for every new connection from clients
     */
    @Override
    public void run() {

        synchronized(this){ this.runningThread = Thread.currentThread(); }

        openServerSocket();
        while(!isStopped){
            Socket clientSocket = null;
            try{
                clientSocket = this.serverSocket.accept();
            }catch (IOException e){
                if(isStopped){
                    System.out.println("Server has stopped");
                    break;
                }
                throw new RuntimeException("Error accepting client connection", e);
            }
            System.out.println("New client: "+clientSocket.getLocalAddress());
            this.threadPool.execute(new ServerThread(clientSocket));
        }

        this.threadPool.shutdown();
        try {
            this.threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            System.out.println("Server Stopped.") ;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    /**
     * Opens the server socket with designated port
     */
    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
//            System.out.println("Server has opened on port "+this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port "+this.serverPort, e);
        }
    }
}
