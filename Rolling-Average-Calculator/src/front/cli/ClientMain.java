package front.cli;

import back.interfacing.ClientUI;
import back.network.ClientAdapter;
import front.cli.utility.indicators.ProgressIndicator;
import front.cli.utility.indicators.SpinningProgressIndicator;
import front.network.ClientMenuState;

import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A Command Line Interface to control an instance of
 * {@link ClientAdapter}. A user will launch this.
 */
public class ClientMain implements ClientUI {

    private static final int INPUT_DELAY_MS = 200;

    private ClientAdapter clientAdapter;
    private ProgressIndicator progressIndicator;
    private Scanner scanner;
    private ClientMenuState menuState;

    /**
     * This lock should be used any time the {@link ClientMenuState} may
     * change, which is likely any time the user is asked for input.
     */
    private ReentrantLock stateLock;
    
    private String additionalText;

    private boolean hasNewInput;
    private boolean shouldQuit;

    private ClientMain() {
        clientAdapter = new ClientAdapter();
        clientAdapter.setUIHandler(this);
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
                if (progressIndicator != null) {
                    while (!stateLock.isHeldByCurrentThread()) {
                        stateLock.lock();
                    }
                    progressIndicator.stop();
                    stateLock.unlock();
                }
                if (additionalText != null) {
                    System.out.println(additionalText);
                    additionalText = null;
                }
                if (shouldQuit) {
                    clientAdapter.disconnect();
                    System.out.println("Good bye!");
                    return;
                }
                processMenuState();
            } else {
                while (!stateLock.isHeldByCurrentThread()) {
                    stateLock.lock();
                }
                if (progressIndicator == null) {
                    progressIndicator = new SpinningProgressIndicator();
                    progressIndicator.begin();
                } else {
                    progressIndicator.next();
                }
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

    @Override
    public void onConnectionSuccess() {
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        additionalText = "Connected!";
        menuState = ClientMenuState.MainMenu;
        hasNewInput = true;
        stateLock.unlock();
    }

    @Override
    public void onConnectionFailure(final String reason) {
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        additionalText = "Failed to connect due to: " + reason;
        menuState = ClientMenuState.PromptReconnect;
        hasNewInput = true;
        stateLock.unlock();
    }

    @Override
    public void onConnectionBroken(final String reason) {
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        additionalText = "Connection failed due to: " + reason;
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
                clientAdapter.connect(ipAddress, port);
                hasNewInput = false;
                break;
            case MainMenu:
                System.out.println();
                System.out.println("Main Menu:");
                System.out.println("Options:");
                System.out.println("1) Submit Number");
                System.out.println("2) Close Connection");
                System.out.println(
                        "\nWhat would you like to do? (number only) ");
                final String input = scanner.nextLine();
                if (input.length() == 1) {
                    if (input.equalsIgnoreCase("1")) {
                        System.out.println("\nEnter a number: ");
                        final String numInput = scanner.nextLine();
                        clientAdapter.sendMessage(numInput);
                        break;
                    } else if (input.equalsIgnoreCase("2")) {
                        clientAdapter.disconnect();
                        hasNewInput = false;
                        break;
                    }
                }
                System.out.print("Sorry, I didn't understand that. ");
                System.out.println("Let me try again");
                break;
            case PromptReconnect:
            case ConnectionFailed:
                if (requestYesNoInput(
                    "Would you like to try connecting again?")) {
                    menuState = ClientMenuState.RequestServerInfo;
                } else {
                    shouldQuit = true;
                }
                hasNewInput = true;
                break;
        }
        if (stateLock.isHeldByCurrentThread()) {
            stateLock.unlock();
        }
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
     * Create and launch the main networking {@link ClientAdapter} and
     * show options to user.
     *
     * @param args The user inputted command line arguments.
     */
    public static void main(final String args[]) {
        System.out.println("Hello, World!");
        new ClientMain().startCommunicating();
    }
}
