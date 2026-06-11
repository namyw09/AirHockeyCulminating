import java.awt.Color;

// paddle variant that can snap its center to the mouse while staying in bounds
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
     * moves the paddle center to the cursor position
     * pre:  minX, maxX, minY, maxY define the allowed rink area
     * post: paddle center follows the cursor but stays inside the allowed area
     */
    public void followCursor(int cursorX, int cursorY,
            int minX, int maxX, int minY, int maxY) {

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
        repaint();
    }
}
