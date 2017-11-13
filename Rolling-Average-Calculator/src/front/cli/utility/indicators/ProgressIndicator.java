package front.cli.utility.indicators;

/**
 * A command line indicator of progress. This serves as a form of busy waiting that does not allow for user input.
 */
public abstract class ProgressIndicator {
    
    /**
     * Create any necessary components and begin managing progress state. Show the first point of progress.
     */
    public abstract void begin();
    
    /**
     * Update to show the next state of progress and adjust to new state.
     */
    public abstract void next();
    
    /**
     * Continue a previously stopped state of progress.
     */
    public abstract void resume();
    
    /**
     * Stop a state of progress and prevent incrementing.
     */
    public abstract void stop();
}
