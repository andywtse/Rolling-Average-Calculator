package front.cli.utility.indicators;

/**
 * A {@link ProgressIndicator} intended to take a single character's width. A bar "spins" clockwise starting from a
 * exactly vertical.
 */
public class SpinningProgressIndicator extends ProgressIndicator {
    
    /**
     * The direction of the "right-facing point". In the case of North, the "right-facing point" is the top.
     */
    private enum SpinState {
        North, NorthEast, East, SouthEast
    }
    
    private SpinState state;
    
    @Override
    public void begin() {
        
        state = SpinState.North;
        next();
    }
    
    @Override
    public void next() {
        
        String output;
        switch (state) {
            case North:
                output = "|";
                state = SpinState.NorthEast;
                break;
            case NorthEast:
                output = "/";
                state = SpinState.East;
                break;
            case East:
                output = "-";
                state = SpinState.SouthEast;
                break;
            case SouthEast:
                output = "\\";
                state = SpinState.North;
                break;
            default:
                output = "";
                break;
        }
        System.out.print("\r" + output);
    }
    
    @Override
    public void resume() {
        
        next();
    }
    
    @Override
    public void stop() {
        
        System.out.print("\r");
    }
}
