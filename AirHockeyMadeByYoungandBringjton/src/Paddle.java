import java.awt.Color;
import java.awt.Graphics;

import framework.GameObject;

// one paddle for one player - moves around their half of the rink
public class Paddle extends GameObject {

    private static final int   PADDLE_WIDTH  = 16;
    private static final int   PADDLE_HEIGHT = 80;
    private static final int   SPEED         = 5;
    private static final float ACCEL         = 1.2f;  // pixels/frame added when key is held
    private static final float DECEL         = 0.75f; // velocity multiplier when key is released

    private Color paddleColor;
    private int   currentSpeed = SPEED;

    private float velX = 0f;
    private float velY = 0f;

    /**
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
     * pre:  minX, maxX, minY, maxY define the area the paddle is allowed to move in
     * post: velocity ramps toward the pressed direction and coasts to a stop when
     *       released; paddle stays within the given bounds
     */
    public void move(boolean up, boolean down, boolean left, boolean right,
            int minX, int maxX, int minY, int maxY) {

        // accelerate or decelerate on the X axis
        if (left && !right) {
            velX = Math.max(velX - ACCEL, -currentSpeed);
        } else if (right && !left) {
            velX = Math.min(velX + ACCEL, currentSpeed);
        } else {
            velX *= DECEL;
        }

        // accelerate or decelerate on the Y axis
        if (up && !down) {
            velY = Math.max(velY - ACCEL, -currentSpeed);
        } else if (down && !up) {
            velY = Math.min(velY + ACCEL, currentSpeed);
        } else {
            velY *= DECEL;
        }

        int centerX = getX() + PADDLE_WIDTH / 2 + (int) velX;
        int centerY = getY() + getHeight() / 2 + (int) velY;

        // keep the paddle inside its allowed area
        if (centerX - PADDLE_WIDTH / 2 < minX) {
            centerX = minX + PADDLE_WIDTH / 2;
            velX = 0;
        }
        if (centerX + PADDLE_WIDTH / 2 > maxX) {
            centerX = maxX - PADDLE_WIDTH / 2;
            velX = 0;
        }
        if (centerY - getHeight() / 2 < minY) {
            centerY = minY + getHeight() / 2;
            velY = 0;
        }
        if (centerY + getHeight() / 2 > maxY) {
            centerY = maxY - getHeight() / 2;
            velY = 0;
        }

        setX(centerX - PADDLE_WIDTH / 2);
        setY(centerY - getHeight() / 2);
        repaint();
    }

    // post: returns current horizontal velocity in pixels/frame (for puck collision math)
    public int getVelocityX() {
        return (int) velX;
    }

    // post: returns current vertical velocity in pixels/frame (for puck collision math)
    public int getVelocityY() {
        return (int) velY;
    }

    /**
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
     * pre:  paddle exists
     * post: currentSpeed is set to 1.5x normal; paddle moves faster until revertSpeed()
     */
    public void speedUp() {
        currentSpeed = SPEED * 3 / 2;
    }

    /**
     * pre:  paddle exists
     * post: currentSpeed is set to 0.75x normal; paddle moves slower until revertSpeed()
     */
    public void slowDown() {
        currentSpeed = SPEED * 3 / 4;
    }

    /**
     * pre:  speedUp() or slowDown() was called
     * post: currentSpeed is restored to the default SPEED
     */
    public void revertSpeed() {
        currentSpeed = SPEED;
    }

    // post: nothing - paddle movement is controlled through move() in AirHockeyGame
    public void act() {
    }

    /**
     * pre:  g is a valid Graphics object
     * post: paddle is drawn with a white border and the player's color inside
     */
    public void paint(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRoundRect(0, 0, PADDLE_WIDTH, getHeight(), 8, 8);

        g.setColor(paddleColor);
        g.fillRoundRect(3, 3, PADDLE_WIDTH - 6, getHeight() - 6, 6, 6);
    }
}
