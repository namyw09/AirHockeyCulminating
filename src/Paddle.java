import java.awt.Color;
import java.awt.Graphics;

import framework.GameObject;

// one paddle for one player - moves around their half of the rink
public class Paddle extends GameObject {

    private static final int PADDLE_WIDTH  = 16;
    private static final int PADDLE_HEIGHT = 80;
    private static final int SPEED = 5;
    private static final int HIT_FLASH_MS = 140;

    private Color paddleColor;
    private int currentSpeed = SPEED;
    private long hitFlashStart = 0;

    // how far the paddle center moved on the most recent frame (its swing speed)
    private int velocityX = 0;
    private int velocityY = 0;

    /**
     * creates a paddle at a center point
     * pre:  centerX and centerY are valid positions on screen, color is not null
     * post: paddle is created, sized, and placed so its center is at (centerX, centerY)
     */
    public Paddle(int centerX, int centerY, Color color) {
        paddleColor = color;
        setSize(PADDLE_WIDTH, PADDLE_HEIGHT);
        setX(centerX - PADDLE_WIDTH / 2);
        setY(centerY - PADDLE_HEIGHT / 2);
    }

    /**
     * moves the paddle from keyboard input
     * pre:  minX, maxX, minY, maxY define the area the paddle is allowed to move in
     * post: paddle moves in the direction of any held keys, and stays within the given bounds
     */
    public void move(boolean up, boolean down, boolean left, boolean right,
            int minX, int maxX, int minY, int maxY) {

        int startCenterX = getX() + PADDLE_WIDTH / 2;
        int startCenterY = getY() + getHeight() / 2;
        int centerX = startCenterX;
        int centerY = startCenterY;

        if (up)    { centerY = centerY - currentSpeed; }
        if (down)  { centerY = centerY + currentSpeed; }
        if (left)  { centerX = centerX - currentSpeed; }
        if (right) { centerX = centerX + currentSpeed; }

        // keep the paddle inside its allowed area
        if (centerX - PADDLE_WIDTH / 2 < minX) {
            centerX = minX + PADDLE_WIDTH / 2;
        }
        if (centerX + PADDLE_WIDTH / 2 > maxX) {
            centerX = maxX - PADDLE_WIDTH / 2;
        }
        if (centerY - getHeight() / 2 < minY) {
            centerY = minY + getHeight() / 2;
        }
        if (centerY + getHeight() / 2 > maxY) {
            centerY = maxY - getHeight() / 2;
        }

        setX(centerX - PADDLE_WIDTH / 2);
        setY(centerY - getHeight() / 2);
        velocityX = centerX - startCenterX;
        velocityY = centerY - startCenterY;
        repaint();
    }

    /**
     * records how far the paddle center moved this frame
     * pre:  vx and vy are the change in center x and y for the current frame
     * post: the paddle remembers its swing velocity for puck collisions
     */
    protected void setVelocity(int vx, int vy) {
        velocityX = vx;
        velocityY = vy;
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
     * makes the paddle taller
     * pre:  paddle is on screen
     * post: paddle height grows to 1.5x; vertical center is preserved
     */
    public void grow() {
        int centerY = getY() + getHeight() / 2;
        setSize(PADDLE_WIDTH, PADDLE_HEIGHT * 3 / 2);
        setY(centerY - getHeight() / 2);
        repaint();
    }

    /**
     * returns the paddle to normal height
     * pre:  paddle has been grown via grow()
     * post: paddle height returns to PADDLE_HEIGHT; vertical center is preserved
     */
    public void revert() {
        int centerY = getY() + getHeight() / 2;
        setSize(PADDLE_WIDTH, PADDLE_HEIGHT);
        setY(centerY - getHeight() / 2);
        repaint();
    }

    /**
     * makes the paddle move faster
     * pre:  paddle exists
     * post: currentSpeed is set to 1.5x normal; paddle moves faster until revertSpeed()
     */
    public void speedUp() {
        currentSpeed = SPEED * 3 / 2;
    }

    /**
     * makes the paddle move slower
     * pre:  paddle exists
     * post: currentSpeed is set to 0.75x normal; paddle moves slower until revertSpeed()
     */
    public void slowDown() {
        currentSpeed = SPEED * 3 / 4;
    }

    /**
     * returns the paddle speed to normal
     * pre:  speedUp() or slowDown() was called
     * post: currentSpeed is restored to the default SPEED
     */
    public void revertSpeed() {
        currentSpeed = SPEED;
    }

    /**
     * briefly highlights the paddle after hitting a puck
     * pre:  paddle exists on screen
     * post: the next paint calls draw a brighter paddle for a short time
     */
    public void flashHit() {
        hitFlashStart = System.currentTimeMillis();
        repaint();
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
        boolean flashing = System.currentTimeMillis() - hitFlashStart < HIT_FLASH_MS;

        if (flashing) {
            g.setColor(new Color(255, 255, 180));
        } else {
            g.setColor(Color.WHITE);
        }
        g.fillRoundRect(0, 0, PADDLE_WIDTH, getHeight(), 8, 8);

        if (flashing) {
            g.setColor(paddleColor.brighter());
        } else {
            g.setColor(paddleColor);
        }
        g.fillRoundRect(3, 3, PADDLE_WIDTH - 6, getHeight() - 6, 6, 6);
    }
}
