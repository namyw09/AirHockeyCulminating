import java.awt.Color;
import java.awt.Graphics;

import framework.GameObject;

// same basic paddle idea from the milestone, now using framework.GameObject
// one paddle for one player - they can only move it around their own half of the rink
public class Paddle extends GameObject {

    // base sizes for the original 800x600 layout; scaled up to fit the window
    private static final int BASE_WIDTH  = 16;
    private static final int BASE_HEIGHT = 80;
    private static final int BASE_SPEED  = 5;

    private final int paddleWidth;
    private final int baseHeight;
    private final int baseSpeed;

    private Color paddleColor;
    private int currentSpeed;

    // how far the paddle moved last frame - basically how hard you're swinging it.
    // we need this so a fast swing actually launches the puck instead of tapping it
    private int velocityX = 0;
    private int velocityY = 0;

    /**
     * creates a paddle at a center point
     * pre:  centerX and centerY are valid positions on screen, color is not null,
     *       scale is positive (1.0 = original size)
     * post: paddle is created, sized by scale, and centered at (centerX, centerY)
     */
    public Paddle(int centerX, int centerY, Color color, double scale) {
        paddleColor = color;
        paddleWidth = Math.max(4, (int) Math.round(BASE_WIDTH * scale));
        baseHeight  = Math.max(20, (int) Math.round(BASE_HEIGHT * scale));
        baseSpeed   = Math.max(1, (int) Math.round(BASE_SPEED * scale));
        currentSpeed = baseSpeed;
        setSize(paddleWidth, baseHeight);
        setX(centerX - paddleWidth / 2);
        setY(centerY - baseHeight / 2);
    }

    /**
     * moves the paddle from keyboard input
     * pre:  minX, maxX, minY, maxY define the area the paddle is allowed to move in
     * post: paddle moves in the direction of any held keys, and stays within the given bounds
     */
    public void move(boolean up, boolean down, boolean left, boolean right,
            int minX, int maxX, int minY, int maxY) {

        int centerX = getCenterX();
        int centerY = getCenterY();

        if (up)    { centerY = centerY - currentSpeed; }
        if (down)  { centerY = centerY + currentSpeed; }
        if (left)  { centerX = centerX - currentSpeed; }
        if (right) { centerX = centerX + currentSpeed; }

        moveCenterTo(centerX, centerY, minX, maxX, minY, maxY);
    }

    /**
     * gets the paddle center x
     * pre:  paddle exists
     * post: returns the x coordinate of the paddle's center
     */
    private int getCenterX() {
        return getX() + getWidth() / 2;
    }

    /**
     * gets the paddle center y
     * pre:  paddle exists
     * post: returns the y coordinate of the paddle's center
     */
    private int getCenterY() {
        return getY() + getHeight() / 2;
    }

    /**
     * moves the paddle center, then keeps it inside the allowed area
     * pre:  minX, maxX, minY, maxY define the allowed rink area
     * post: paddle moves as close as possible to the requested center point
     */
    private void moveCenterTo(int centerX, int centerY, int minX, int maxX, int minY, int maxY) {
        int startCenterX = getCenterX();
        int startCenterY = getCenterY();
        int halfWidth = getWidth() / 2;
        int halfHeight = getHeight() / 2;

        // don't let the paddle escape its own half
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
        velocityX = centerX - startCenterX;
        velocityY = centerY - startCenterY;
        repaint();
    }

    /**
     * gets how fast the paddle is moving sideways this frame
     * pre:  paddle exists
     * post: returns the paddle center's horizontal change for the current frame
     */
    public int getVelocityX() {
        return velocityX;
    }

    /**
     * gets how fast the paddle is moving up or down this frame
     * pre:  paddle exists
     * post: returns the paddle center's vertical change for the current frame
     */
    public int getVelocityY() {
        return velocityY;
    }

    /**
     * makes the paddle taller (the size powerup)
     * pre:  paddle is on screen
     * post: paddle gets 1.5x taller but stays centered where it was, so it doesn't
     *       suddenly jump up or down
     */
    public void grow() {
        int centerY = getY() + getHeight() / 2;
        setSize(paddleWidth, baseHeight * 3 / 2);
        setY(centerY - getHeight() / 2);
        repaint();
    }

    /**
     * returns the paddle to normal height
     * pre:  paddle has been grown via grow()
     * post: paddle height returns to baseHeight; vertical center is preserved
     */
    public void revert() {
        int centerY = getY() + getHeight() / 2;
        setSize(paddleWidth, baseHeight);
        setY(centerY - getHeight() / 2);
        repaint();
    }

    /**
     * makes the paddle zippier (the speed powerup)
     * pre:  paddle exists
     * post: bumps the paddle's speed to 1.5x normal until revertSpeed() puts it back
     */
    public void speedUp() {
        currentSpeed = baseSpeed * 3 / 2;
    }

    /**
     * slows the paddle down (this one gets used on your opponent, hehe)
     * pre:  paddle exists
     * post: drops the paddle's speed to 0.75x normal until revertSpeed() fixes it
     */
    public void slowDown() {
        currentSpeed = baseSpeed * 3 / 4;
    }

    /**
     * returns the paddle speed to normal
     * pre:  speedUp() or slowDown() was called
     * post: currentSpeed is restored to the default baseSpeed
     */
    public void revertSpeed() {
        currentSpeed = baseSpeed;
    }

    /**
     * leaves paddle frame behavior to AirHockeyGame
     * pre:  none
     * post: nothing - paddle movement is controlled through move() in AirHockeyGame
     */
    public void act() {
    }

    /**
     * draws the paddle
     * pre:  g is a valid Graphics object
     * post: paddle is drawn with a white border and the player's color inside
     */
    public void paint(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRoundRect(0, 0, paddleWidth, getHeight(), 8, 8);

        g.setColor(paddleColor);
        g.fillRoundRect(3, 3, paddleWidth - 6, getHeight() - 6, 6, 6);
    }
}
