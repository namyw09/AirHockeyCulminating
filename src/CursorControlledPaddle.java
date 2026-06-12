import java.awt.Color;

// a special paddle that follows your mouse instead of the keys - this is the reward
// you get for winning the candy battle. still can't leave its own half though
public class CursorControlledPaddle extends Paddle {

    /**
     * creates a paddle that can follow the cursor
     * pre:  centerX and centerY are valid positions on screen, color is not null
     * post: cursor controlled paddle is created like a normal paddle
     */
    public CursorControlledPaddle(int centerX, int centerY, Color color) {
        super(centerX, centerY, color);
    }

    /**
     * snaps the paddle to wherever the mouse is
     * pre:  minX, maxX, minY, maxY define the allowed rink area
     * post: the paddle jumps to the cursor, but we still clamp it so it can't slide
     *       out of its half of the rink
     */
    public void followCursor(int cursorX, int cursorY,
            int minX, int maxX, int minY, int maxY) {

        int oldCenterX = getX() + getWidth() / 2;
        int oldCenterY = getY() + getHeight() / 2;

        int centerX = cursorX;
        int centerY = cursorY;
        int halfWidth = getWidth() / 2;
        int halfHeight = getHeight() / 2;

        if (centerX - halfWidth < minX) {
            centerX = minX + halfWidth;
        }
        if (centerX + halfWidth > maxX) {
            centerX = maxX - halfWidth;
        }
        if (centerY - halfHeight < minY) {
            centerY = minY + halfHeight;
        }
        if (centerY + halfHeight > maxY) {
            centerY = maxY - halfHeight;
        }

        setX(centerX - halfWidth);
        setY(centerY - halfHeight);
        setVelocity(centerX - oldCenterX, centerY - oldCenterY);
        repaint();
    }
}
