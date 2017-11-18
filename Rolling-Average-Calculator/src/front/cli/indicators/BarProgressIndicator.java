package front.cli.indicators;

import java.util.List;

/**
 * A {@link ProgressIndicator} intended to take a set 80 characters of space. The entire row is written and rewritten
 * with an inputted character or set of characters to indicate continued progress.
 */
public class BarProgressIndicator extends ProgressIndicator {
    
    private static final int MAX_WIDTH_CHAR_COUNT = 78;
    
    private List<Character> displayCharacters;
    
    private int currentBarCount = 0;
    private int currentBarIndex = 0;
    
    private boolean shouldAlternate;
    
    public BarProgressIndicator( final List<Character> characters ) {
        
        this.displayCharacters = characters;
        this.shouldAlternate = this.displayCharacters.size() > 1;
    }
    
    @Override
    public void begin() {
        
        next();
    }
    
    @Override
    public void next() {
        
        if (displayCharacters.size() == 0) {
            return;
        }
        System.out.print("\r[");
        
        final int existingCount;
        if (shouldAlternate) {
            existingCount = currentBarCount;
        } else {
            existingCount = currentBarCount - 1;
        }
        
        for (int i = 0; i < existingCount; ++i) {
            System.out.print(displayCharacters.get(displayCharacters.size() - 1));
        }
        
        if (shouldAlternate) {
            System.out.print(displayCharacters.get(currentBarIndex));
            currentBarIndex = ( currentBarIndex + 1 ) % displayCharacters.size();
        }
        
        for (int i = currentBarCount; i < MAX_WIDTH_CHAR_COUNT - 1; ++i) {
            System.out.print(" ");
        }
        
        System.out.print("]");
        System.out.flush();
        
        if (!shouldAlternate || currentBarIndex == 0) {
            currentBarCount = ( currentBarCount + 1 ) % MAX_WIDTH_CHAR_COUNT;
        }
    }
    
    @Override
    public void resume() {
        
        next();
    }
    
    @Override
    public void stop() {
        
        System.out.println("\r");
    }
}
