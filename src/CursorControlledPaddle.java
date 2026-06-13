import java.awt.Color;

// a special paddle that follows your mouse instead of the keys: reward for winning the candy battle
public class CursorControlledPaddle extends Paddle {

    /**
     * creates a paddle that can follow the cursor
     * pre:  centerX and centerY are valid positions on screen, color is not null,
     *       scale is positive (1.0 = original size)
     * post: cursor controlled paddle is created like a normal paddle, sized by scale
     */
    public CursorControlledPaddle(int centerX, int centerY, Color color, double scale) {
        super(centerX, centerY, color, scale);
    }

    /**
     * moves the paddle to wherever the mouse is
     * pre:  minX, maxX, minY, maxY define the allowed rink area
     * post: the paddle jumps to the cursor, but we still clamp it so it can't slide
     *       out of its half of the rink
     */
    public void followCursor(int cursorX, int cursorY,
            int minX, int maxX, int minY, int maxY) {

        moveCenterTo(cursorX, cursorY, minX, maxX, minY, maxY);
    }
}
