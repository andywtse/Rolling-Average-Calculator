package back.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThread implements Runnable{

    protected Socket clientSocket = null;

    public ServerThread(Socket clientSocket){
        this.clientSocket=clientSocket;
    }

    public void run(){

        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            out.println("Welcome to the server");

            //Process information

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            this.clientSocket.close();
        }catch (IOException e){
            System.out.println("Error on closing client connection");
        }
    }
}
