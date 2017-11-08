package front.cli;

import back.interfacing.ClientUI;
import back.network.Client;
import front.network.ClientMenuState;

import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A Command Line Interface to control an instance of
 * {@link Client}. A user will launch this.
 */
public class ClientMain implements ClientUI {

    private static final int INPUT_DELAY_MS = 500;

    private Client client;
    private Scanner scanner;
    private ClientMenuState menuState;

    /**
     * This lock should be used any time the {@link ClientMenuState} may
     * change, which is likely any time the user is asked for input.
     */
    private ReentrantLock stateLock;

    private boolean hasNewInput;
    private boolean shouldQuit;

    private ClientMain() {
        client = new Client();
        client.setUIHandler(this);
        scanner = new Scanner(System.in);
        stateLock = new ReentrantLock();
        menuState = ClientMenuState.RequestServerInfo;
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
                    client.disconnect();
                    System.out.println("Good bye!");
                    return;
                }
                processMenuState();
            } else {
                System.out.println("Processing...");
                System.out.print("While you wait, I'll echo your input: ");
                final String input = scanner.nextLine();
                System.out.println("echo: " + input);
            }

            try {
                Thread.sleep(INPUT_DELAY_MS);
            } catch (InterruptedException e) {
                System.err.println("Something went wrong!");
                System.err.println(e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void onConnectionSuccess() {
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        System.out.println("Connected!");
        menuState = ClientMenuState.MainMenu;
        hasNewInput = true;
        stateLock.unlock();
    }

    @Override
    public void onConnectionFailure(final String reason) {
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        System.out.println("Failed to connect due to: " + reason);
        menuState = ClientMenuState.PromptReconnect;
        hasNewInput = true;
        stateLock.unlock();
    }

    @Override
    public void onConnectionBroken(final String reason) {
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        System.out.println("Connection failed due to: " + reason);
        menuState = ClientMenuState.ConnectionFailed;
        hasNewInput = true;
        stateLock.unlock();
    }

    /**
     * Process the current {@link ClientMenuState}. Depending on the option
     * selected by the user, there may be additional submenus, additional
     * required information, or simple waiting until the server responds.
     */
    private void processMenuState() {
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        switch (menuState) {
            case RequestServerInfo:
                System.out.print("What is the IP address you want to connect to? ");
                final String ipAddress = scanner.nextLine();
                System.out.print("What port are you connecting to? ");
                final String port = scanner.nextLine();
                client.connect(ipAddress, port);
                hasNewInput = false;
                break;
            case MainMenu:
                System.out.println("Main Menu:");
                // TODO 11/07/17: Fill this in with more options.
                break;
            case PromptReconnect:
            case ConnectionFailed:
                requestYesNoInput("Would you like to try connecting again?");
                break;
        }
        if (stateLock.isHeldByCurrentThread()) {
            stateLock.unlock();
        }
    }

    /**
     * Request a proper "y" or "n" answer from the user for a specific response.
     *
     * @param requestMessage The message to request a "Yes" or "No" answer.
     */
    private void requestYesNoInput(final String requestMessage) {
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        do {
            System.out.print(requestMessage + " (y/n) ");
            final String input = scanner.nextLine();
            if (input.length() == 1) {
                if (input.equalsIgnoreCase("Y")) {
                    menuState = ClientMenuState.RequestServerInfo;
                    break;
                } else if (input.equalsIgnoreCase("N")) {
                    shouldQuit = true;
                    break;
                }
            }
            System.out.println("Sorry, I couldn't understand you. Let me try again.");
        } while (true);
        hasNewInput = true;
        stateLock.unlock();
    }

    /**
     * Create and launch the main networking {@link Client} and
     * show options to user.
     *
     * @param args The user inputted command line arguments.
     */
    public static void main(final String args[]) {
        System.out.println("Hello, World!");
        new ClientMain().startCommunicating();
    }
}
