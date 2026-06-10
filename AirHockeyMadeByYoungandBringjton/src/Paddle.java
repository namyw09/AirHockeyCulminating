import java.awt.Color;
import java.awt.Graphics;

import framework.GameObject;

// one paddle for one player - moves around their half of the rink
public class Paddle extends GameObject {

    private static final int PADDLE_WIDTH  = 16;
    private static final int PADDLE_HEIGHT = 80;
    private static final int SPEED = 5;

    private Color paddleColor;
    private int currentSpeed = SPEED;

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
     * post: paddle moves in the direction of any held keys, and stays within the given bounds
     */
    public void move(boolean up, boolean down, boolean left, boolean right,
            int minX, int maxX, int minY, int maxY) {

        int centerX = getX() + PADDLE_WIDTH / 2;
        int centerY = getY() + getHeight() / 2;

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
        repaint();
    }

    /**
     * pre:  paddle is on screen
     * post: paddle height doubles to PADDLE_HEIGHT * 2; vertical center is preserved
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
     * post: currentSpeed is doubled; paddle moves 2x faster until revertSpeed() is called
     */
    public void speedUp() {
        currentSpeed = SPEED * 3 / 2; // 1.5x (int: 5 * 3/2 = 7)
    }

    /**
     * pre:  paddle exists
     * post: currentSpeed is halved; paddle moves at half speed until revertSpeed() is called
     */
    public void slowDown() {
        currentSpeed = SPEED * 3 / 4; // 0.75x (int: 5 * 3/4 = 3)
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
