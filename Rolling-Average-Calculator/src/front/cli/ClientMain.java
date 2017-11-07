package front.cli;

import back.interfacing.ClientUI;
import back.network.Client;

import java.util.Scanner;

/**
 * A Command Line Interface to control an instance of
 * {@link Client}. A user will launch this.
 */
public class ClientMain implements ClientUI {

    private Client client;
    private Scanner scanner;

    private ClientMain() {
        client = new Client();
        client.setUIHandler(this);
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
     * Create and launch the main networking {@link Client} and
     * show options to user.
     *
     * @param args The user inputted command line arguments
     */
    public static void main(final String args[]) {
        System.out.println("Hello, World!");
        new ClientMain().startCommunicating();
    }
}
