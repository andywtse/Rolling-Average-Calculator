package front.cli;

import back.interfacing.ServerUI;
import back.network.Server;

import java.util.Scanner;

/**
 * A Command Line Interface to control an instance of
 * {@link back.network.Server}. A user will launch this.
 */
public class ServerMain implements ServerUI {

    private Server server;
    private Scanner scanner;

    private ServerMain() {
        server = new Server();
        server.setUIHandler(this);
        scanner = new Scanner(System.in);
    }

    /**
     * Perform I/O with user.
     */
    private void startCommunicating() {
        while (true) {
            System.out.println("What would you like to do?");
            final String input = scanner.nextLine();
            System.out.println("echo: " + input);
        }
    }

    /**
     * Create and launch the main networking {@link Server} and
     * show options to user.
     *
     * @param args The user inputted command line arguments
     */
    public static void main(final String args[]) {
        System.out.println("Hello, World!");
        new ServerMain().startCommunicating();
    }
}
