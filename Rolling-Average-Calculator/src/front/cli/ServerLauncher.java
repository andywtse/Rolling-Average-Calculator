package front.cli;

import back.interfacing.ServerUI;
import back.network.server.ServerAdapter;
import front.cli.indicators.BarProgressIndicator;
import front.cli.indicators.ProgressIndicator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A Command Line Interface to control an instance of
 * {@link ServerAdapter}. A user will launch this.
 */
public class ServerLauncher implements ServerUI {
    
    private static final int INPUT_DELAY_MS = 200;
    private List<String> log = new LinkedList<>();
    private ServerAdapter serverAdapter = new ServerAdapter();
    private ProgressIndicator progressIndicator;
    private Scanner scanner = new Scanner(System.in);
    private MenuState menuState = MenuState.RequestServerInfo;
    /**
     * This lock should be used any time the
     * {@link MenuState} may change, which is likely any
     * time the user is asked for input.
     */
    private ReentrantLock stateLock = new ReentrantLock();
    private boolean hasNewInput = true;
    private boolean shouldQuit = false;
    
    private ServerLauncher() {
        
        serverAdapter.setUIHandler(this);
        final List<Character> list = new ArrayList<>();
        list.add('-');
        list.add('=');
        progressIndicator = new BarProgressIndicator(list);
    }
    
    /**
     * Create and launch the main networking {@link ServerAdapter} and
     * show options to user.
     *
     * @param args The user inputted command line arguments
     */
    public static void main( final String args[] ) {
        
        System.out.println("Hello, World!");
        new ServerLauncher().startCommunicating();
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
                processMenuState();
            } else {
                if (!shouldQuit) {
                    progressIndicator.next();
                }
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
        serverAdapter.shutDown();
        System.out.println("Good bye!");
    }
    
    /**
     * Process the current {@link MenuState}. Depending on the option
     * selected by the user, there may be additional submenus, additional
     * required information, or simple waiting until the serverAdapter responds.
     */
    private void processMenuState() {
        
        switch (menuState) {
            case RequestServerInfo:
                System.out.print("What is your current IP address? ");
                final String ipAddress = "127.0.0.1";//scanner.nextLine();
                System.out.print("What port are you opening? ");
                final String port = "8080";//scanner.nextLine();
                serverAdapter.spinUp(ipAddress, port);
                hasNewInput = false;
                break;
            case MainMenu:
                // TODO 11/07/17: Fill this in with more options.
                System.out.println("Main Menu");
                System.out.println("Options:");
                System.out.println("1) Output logs");
                System.out.println("2) Shutdown");
                System.out.println("\nWhat would you like to do? (number only) ");
                final String input = scanner.nextLine();
                if (input.length() == 1) {
                    if (input.equalsIgnoreCase("1")) {
                        System.out.println("Logs:\n");
                        for (int i = 0; i < log.size(); ++i) {
                            System.out.println(( i + 1 ) + ") " + log.get(i));
                        }
                        System.out.println("\n Logs complete");
                        break;
                    } else if (input.equalsIgnoreCase("2")) {
                        serverAdapter.shutDown();
                        hasNewInput = false;
                        break;
                    }
                }
                System.out.println("Sorry, I didn't understand that.");
                System.out.println("Let me try again");
                break;
        }
    }
    
    @Override
    public void onSpinUpSuccess() {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String started = "ServerAdapter started!";
        log.add(started);
        System.out.println(started);
        menuState = MenuState.MainMenu;
        hasNewInput = true;
        stateLock.unlock();
    }
    
    @Override
    public void onSpinUpFailure( final String reason ) {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String failure = "Could not begin serverAdapter due to: " + reason;
        log.add(failure);
        System.err.println(failure);
        if (requestYesNoInput("Would you like to try again?")) {
            menuState = MenuState.RequestServerInfo;
        } else {
            shouldQuit = true;
        }
        hasNewInput = true;
        stateLock.unlock();
    }
    
    @Override
    public void onClientConnected( final String ipAddress ) {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        log.add("ClientAdapter with IP address " + ipAddress + " has connected!");
        stateLock.unlock();
    }
    
    @Override
    public void onClientDisconnected( final String ipAddress ) {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        log.add("ClientAdapter with IP address " + ipAddress + " has disconnected!");
        stateLock.unlock();
    }
    
    @Override
    public void onShutdownSuccess() {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String success = "All connections have stopped.";
        log.add(success);
        System.out.println(success);
        if (shouldQuit) {
            return;
        }
        
        if (requestYesNoInput("\nWould you like to restart the serverAdapter?")) {
            menuState = MenuState.RequestServerInfo;
        } else {
            shouldQuit = true;
        }
        hasNewInput = true;
        if (stateLock.isHeldByCurrentThread()) {
            stateLock.unlock();
        }
    }
    
    @Override
    public void onShutdownFailure( final String reason ) {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String failure = "Failed to shutdown serverAdapter due to: " + reason;
        log.add(failure);
        System.err.println(failure);
        menuState = MenuState.MainMenu;
        shouldQuit = false;
        hasNewInput = true;
        stateLock.unlock();
    }
    
    @Override
    public void onConnectionBroken( final String reason ) {
        
        while (!stateLock.isHeldByCurrentThread()) {
            stateLock.lock();
        }
        final String connectionBroken = "Connection to a client has failed due to " + reason;
        log.add(connectionBroken);
        stateLock.unlock();
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
     * The menu state of the user-facing server. This is used to determine
     * what messages to show to the user and what kind of interaction to
     * request from them.
     */
    private enum MenuState {
        RequestServerInfo, MainMenu,
    }
}
