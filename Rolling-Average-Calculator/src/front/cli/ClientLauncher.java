package front.cli;

import back.interfacing.ClientUI;
import back.network.client.ClientAdapter;
import front.cli.indicators.ProgressIndicator;
import front.cli.indicators.SpinningProgressIndicator;

import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A Command Line Interface to control an instance of
 * {@link ClientAdapter}. A user will launch this.
 */
public class ClientLauncher implements ClientUI {
    
    private static final int INPUT_DELAY_MS = 200;
    private ClientAdapter clientAdapter = new ClientAdapter();
    private ProgressIndicator progressIndicator = new SpinningProgressIndicator();
    private Scanner scanner = new Scanner(System.in);
    private MenuState menuState = MenuState.RequestServerInfo;
    /**
     * This lock should be used any time the {@link MenuState} may
     * change, which is likely any time the user is asked for input.
     */
    private ReentrantLock stateLock = new ReentrantLock();
    private String additionalText;
    private boolean hasNewInput = true;
    private boolean shouldQuit = false;
    
    private ClientLauncher() {
        
        clientAdapter.setUIHandler(this);
    }
    
    /**
     * Create and launch the main networking {@link ClientAdapter} and
     * show options to user.
     *
     * @param args The user inputted command line arguments.
     */
    public static void main( final String args[] ) {
        
        System.out.println("Hello, World!");
        new ClientLauncher().startCommunicating();
    }
    
    /**
     * Perform I/O with user.
     * <p>
     * This thread is kept alive to keep references alive and handle all UI
     * operations.
     */
    private void startCommunicating() {
        
        do {
            while (!stateLock.isHeldByCurrentThread()) {
                stateLock.lock();
            }
            if (hasNewInput) {
                if (progressIndicator != null) {
                    progressIndicator.stop();
                }
                if (additionalText != null) {
                    System.out.println(additionalText);
                    additionalText = null;
                }
                processMenuState();
            } else {
                progressIndicator.next();
            }
            
            if (stateLock.isHeldByCurrentThread()) {
                stateLock.unlock();
            }
            
            try {
                Thread.sleep(INPUT_DELAY_MS);
            } catch (InterruptedException e) {
                System.err.println("Something went wrong!");
                System.err.println(e.getLocalizedMessage());
            }
        } while (!shouldQuit);
        
        clientAdapter.disconnect();
        System.out.println("Good bye!");
    }
    
    @Override
    public void onConnectionSuccess() {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        additionalText = "Connected!";
        menuState = MenuState.MainMenu;
        hasNewInput = true;
        stateLock.unlock();
    }
    
    @Override
    public void onConnectionFailure( final String reason ) {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        additionalText = "Failed to connect due to: " + reason;
        menuState = MenuState.PromptReconnect;
        hasNewInput = true;
        stateLock.unlock();
    }
    
    @Override
    public void onConnectionBroken( final String reason ) {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        additionalText = "Connection failed due to: " + reason;
        menuState = MenuState.ConnectionFailed;
        hasNewInput = true;
        stateLock.unlock();
    }
    
    /**
     * Process the current {@link MenuState}. Depending on the option
     * selected by the user, there may be additional submenus, additional
     * required information, or simple waiting until the server responds.
     */
    private void processMenuState() {
        
        switch (menuState) {
            case RequestServerInfo:
                System.out.print("What is the IP address you want to connect to? ");
                final String ipAddress = "127.0.0.1"; //scanner.nextLine();
                System.out.print("What port are you connecting to? ");
                final String port = "8080"; //scanner.nextLine();
                clientAdapter.connect(ipAddress, port);
                hasNewInput = false;
                break;
            case MainMenu:
                System.out.println();
                System.out.println("Main Menu:");
                System.out.println("Options:");
                System.out.println("1) Submit number");
                System.out.println("2) Request average of Server");
                System.out.println("3) Request average of your  numbers");
                System.out.println("4) Request all Submissions from you");
                System.out.println("5) Request all Submissions from everyone on the server");
                System.out.println("6) Request count of your submissions");
                System.out.println("7) Close connection");
                System.out.println("\nWhat would you like to do? (number only) ");
                final String input = scanner.nextLine();
                if (input.length() == 1) {
                    if (input.equalsIgnoreCase("1")) {
                        System.out.println("\nEnter a number: ");
                        final int numInput = scanner.nextInt();
                        ;
//                        while (!scanner.hasNextInt()) {
//                            System.out.println("That's not a number!");
//                            scanner.next(); // this is important!
//                        }
//                        numInput = scanner.nextInt();
                        clientAdapter.sendValue(numInput);
                        break;
                    } else if (input.equalsIgnoreCase("2")) {
                        clientAdapter.sendCommand(".average", ".all");
                        break;
                    } else if (input.equalsIgnoreCase("3")) {
                        clientAdapter.sendCommand(".average", ".self");
                        break;
                    } else if (input.equalsIgnoreCase("4")) {
                        clientAdapter.sendCommand(".history", ".self");
                        break;
                    } else if (input.equalsIgnoreCase("5")) {
                        clientAdapter.sendCommand(".history", ".all");
                        break;
                    } else if (input.equalsIgnoreCase("6")) {
                        clientAdapter.sendCommand(".count", ".self");
                        break;
                    } else if (input.equalsIgnoreCase("7")) {
                        clientAdapter.sendCommand(".disconnect", ".self");
                        clientAdapter.disconnect();
                        hasNewInput = false;
                        break;
                    }
                }
                System.out.println("Sorry, I didn't understand that.");
                System.out.println("Let me try again");
                break;
            case PromptReconnect:
            case ConnectionFailed:
                if (requestYesNoInput("Would you like to try connecting again?")) {
                    menuState = MenuState.RequestServerInfo;
                } else {
                    shouldQuit = true;
                }
                hasNewInput = true;
                break;
        }
    }
    
    /**
     * Request a proper "y" or "n" answer from the user for a specific response.
     * Everyone who calls this should attempt to unlock the stateLock.
     *
     * @param requestMessage The message to request a "Yes" or "No" answer.
     */
    private boolean requestYesNoInput( final String requestMessage ) {
        
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
     * The menu state of the user-facing client. This is used to determine
     * what messages to show to the user and what kind of interaction to
     * request from them.
     */
    private enum MenuState {
        RequestServerInfo, MainMenu, PromptReconnect, ConnectionFailed,
    }
}
