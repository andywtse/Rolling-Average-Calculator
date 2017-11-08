package front.cli;

import back.interfacing.ServerUI;
import back.network.Server;
import front.network.ServerMenuState;

import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A Command Line Interface to control an instance of
 * {@link back.network.Server}. A user will launch this.
 */
public class ServerMain implements ServerUI {

    private static final int INPUT_DELAY_MS = 2000;

    private Server server;
    private Scanner scanner;
    private ServerMenuState menuState;

    /**
     * This lock should be used any time the
     * {@link front.network.ServerMenuState} may change, which is likely any
     * time the user is asked for input.
     */
    private ReentrantLock stateLock;

    private boolean hasNewInput;
    private boolean shouldQuit;

    private ServerMain() {
        server = new Server();
        server.setUIHandler(this);
        scanner = new Scanner(System.in);
        stateLock = new ReentrantLock();
        menuState = ServerMenuState.RequestServerInfo;
        hasNewInput = true;
        shouldQuit = false;
    }

    /**
     * Perform I/O with user.
     *
     * This thread is kept alive to keep references alive and handle all UI
     * operations.
     */
    private void startCommunicating() {
        while (true) {
            if (hasNewInput) {
                if (shouldQuit) {
                    server.shutDown();
                    System.out.println("Good bye!");
                    return;
                }
                processMenuState();
            } else {
                while (!stateLock.isHeldByCurrentThread()) {
                    stateLock.lock();
                }
                System.out.println("Processing...");
                System.out.print("While you wait, I'll echo your input: ");
                final String input = scanner.nextLine();
                System.out.println("echo: " + input);
                stateLock.unlock();
            }

            try {
                Thread.sleep(INPUT_DELAY_MS);
            } catch (InterruptedException e) {
                System.err.println("Something went wrong!");
                System.err.println(e.getLocalizedMessage());
            }
        }
    }

    /**
     * Process the current {@link ServerMenuState}. Depending on the option
     * selected by the user, there may be additional submenus, additional
     * required information, or simple waiting until the server responds.
     */
    private void processMenuState() {
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        switch (menuState) {
            case RequestServerInfo:
                System.out.print("What is your current IP address? ");
                final String ipAddress = scanner.nextLine();
                System.out.print("What port are you opening? ");
                final String port = scanner.nextLine();
                server.spinUp(ipAddress, port);
                hasNewInput = false;
                break;
            case MainMenu:
                // TODO 11/07/17: Fill this in with more options.
                System.out.println("Main Menu");
                System.out.println("Options:");
                System.out.println("1) Shutdown");
                System.out.println(
                    "\nWhat would you like to do? (number only) ");
                final String input = scanner.nextLine();
                if (input.length() == 1) {
                    if (input.equalsIgnoreCase("1")) {
                        server.shutDown();
                        hasNewInput = false;
                        break;
                    }
                }
                System.out.print("Sorry, I didn't understand that. ");
                System.out.println("Let me try again");
                break;
        }
        if (stateLock.isHeldByCurrentThread()) {
            stateLock.unlock();
        }
    }

    @Override
    public void onSpinUpSuccess() {
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        System.out.println("Server started!");
        menuState = ServerMenuState.MainMenu;
        hasNewInput = true;
        stateLock.unlock();
    }

    @Override
    public void onSpinUpFailure(final String reason) {
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        System.out.println("Could not start server due to: " + reason);
        if (requestYesNoInput("Would you like to try again?")) {
            menuState = ServerMenuState.RequestServerInfo;
        } else {
            shouldQuit = true;
        }
        hasNewInput = true;
        stateLock.unlock();
    }

    @Override
    public void onClientConnected(final String ipAddress) {
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        System.out.println("Client with IP address " + ipAddress
            + " has connected!");
        stateLock.unlock();
    }

    @Override
    public void onClientDisconnected(final String ipAddress) {
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        System.out.println("Client with IP address " + ipAddress
                + " has disconnected!");
        stateLock.unlock();
    }

    @Override
    public void onShutdownSuccess() {
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        System.out.println("All connections have stopped.");
        if (shouldQuit) {
            return;
        }

        if (requestYesNoInput(
            "Would you like to restart the server?")) {
            menuState = ServerMenuState.RequestServerInfo;
        } else {
            shouldQuit = true;
        }
        hasNewInput = true;
        if (stateLock.isHeldByCurrentThread()) {
            stateLock.unlock();
        }
    }

    @Override
    public void onShutdownFailure(final String reason) {
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        System.out.println("Failed to shutdown server due to: " + reason);
        menuState = ServerMenuState.MainMenu;
        hasNewInput = true;
        stateLock.unlock();
    }

    @Override
    public void onConnectionBroken(final String reason) {
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        System.out.println("Connection to a client has failed due to "
            + reason);
        stateLock.unlock();
    }

    /**
     * Request a proper "y" or "n" answer from the user for a specific response.
     * Everyone who calls this should attempt to unlock the stateLock.
     *
     * @param requestMessage The message to request a "Yes" or "No" answer.
     */
    private boolean requestYesNoInput(final String requestMessage) {
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        do {
            System.out.print(requestMessage + " (y/n) ");
            final String input = scanner.nextLine();
            if (input.length() == 1) {
                if (input.equalsIgnoreCase("Y")) {
                    return true;
                } else if (input.equalsIgnoreCase("N")) {
                    return false;
                }
            }
            System.out.println("Sorry, I couldn't understand you. Let me try again.");
        } while (true);
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
