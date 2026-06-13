import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

import framework.GameObject;

// the puck - the little disc that flies around and bounces off the walls and paddles
public class Puck extends GameObject {

    // base values for the original 800x600 layout; scaled up to fit the window
    private static final int BASE_RADIUS = 14;
    private static final double BASE_SERVE_SPEED = 11.0;
    private static final double BASE_MIN_SPEED   = 9.5;
    private static final double BASE_MAX_SPEED   = 17.0;
    private static final double PADDLE_HIT_BOOST = 1.14;
    private static final double FRICTION    = 0.998;
    private static final double PADDLE_SPEED_TRANSFER = 0.95;

    // actual sizes/speeds for this match, scaled from the base values above
    private final int    radius;
    private final int    diameter;
    private final double serveSpeed;
    private final double minSpeed;
    private final double maxSpeed;

    // These store decimal positions so movement stays smooth.
    // Swing can only draw the puck at whole-number screen positions.
    private double exactX;
    private double exactY;
    private double xSpeed = 0;
    private double ySpeed = 0;

    /**
     * creates a puck at a center point
     * pre:  centerX and centerY are valid positions inside the rink,
     *       scale is positive (1.0 = original size)
     * post: puck is created, sized by scale, and centered at (centerX, centerY)
     */
    public Puck(int centerX, int centerY, double scale) {
        radius     = Math.max(4, (int) Math.round(BASE_RADIUS * scale));
        diameter   = radius * 2;
        serveSpeed = BASE_SERVE_SPEED * scale;
        minSpeed   = BASE_MIN_SPEED * scale;
        maxSpeed   = BASE_MAX_SPEED * scale;
        setSize(diameter, diameter);
        setCenter(centerX, centerY);
    }

    /**
     * gets the puck center x position
     * pre:  puck exists on screen
     * post: returns the x coordinate of the center of the puck
     */
    public int getCenterX() {
        return getX() + radius;
    }

    /**
     * gets the puck center y position
     * pre:  puck exists on screen
     * post: returns the y coordinate of the center of the puck
     */
    public int getCenterY() {
        return getY() + radius;
    }

    /**
     * gets the puck radius
     * pre:  puck exists
     * post: returns the radius of the puck in pixels
     */
    public int getRadius() {
        return radius;
    }

    /**
     * sets the puck center position
     * pre:  centerX and centerY are valid positions inside the rink
     * post: puck is moved so its center is at (centerX, centerY)
     */
    private void setCenter(int centerX, int centerY) {
        exactX = centerX - radius;
        exactY = centerY - radius;
        setX((int)Math.round(exactX));
        setY((int)Math.round(exactY));
    }

    /**
     * serves the puck in a randomized diagonal direction
     * pre:  direction is either 1 or -1, random is not null
     * post: puck velocity is set to a playable diagonal serve toward direction
     */
    public void serve(int direction, Random random) {
        double randomUpDownSpeed = 0.30 + random.nextDouble() * 0.45;
        boolean goingUp = random.nextBoolean();

        if (goingUp) {
            randomUpDownSpeed = -randomUpDownSpeed;
        }

        xSpeed = serveSpeed * direction;
        ySpeed = serveSpeed * randomUpDownSpeed;
        limitTopSpeed();
    }

    /**
     * bounces the puck off a paddle, adding the paddle's swing and a small speed boost
     * pre:  puck is overlapping or was swept over by the given paddle
     * post: puck is aimed away from the paddle's face, the paddle's swing velocity is
     *       added so a moving hit drives the puck harder, speed increases slightly,
     *       and puck is pushed outside the paddle so it doesn't get stuck
     */
    public void hitByPaddle(Paddle paddle) {
        // Step 1: send the puck away from the paddle.
        if (paddle.getX() + paddle.getWidth() / 2 < getCenterX()) {
            xSpeed = Math.abs(xSpeed);
        } else {
            xSpeed = -Math.abs(xSpeed);
        }

        // Step 2: a moving paddle gives some of its movement to the puck.
        xSpeed = xSpeed + paddle.getVelocityX() * PADDLE_SPEED_TRANSFER;
        ySpeed = ySpeed + paddle.getVelocityY() * PADDLE_SPEED_TRANSFER;

        // Step 3: make sure the puck does not crawl sideways after a hit.
        if (Math.abs(xSpeed) < minSpeed) {
            xSpeed = (xSpeed < 0) ? -minSpeed : minSpeed;
        }

        // Step 4: paddle hits make the puck a little faster.
        xSpeed = xSpeed * PADDLE_HIT_BOOST;
        ySpeed = ySpeed * PADDLE_HIT_BOOST;
        limitTopSpeed();

        // Step 5: move the puck outside the paddle so it does not hit again right away.
        if (xSpeed > 0) {
            exactX = paddle.getX() + paddle.getWidth() + 1;
        } else {
            exactX = paddle.getX() - diameter - 1;
        }

        setX((int)Math.round(exactX));
        repaint();
    }

    /**
     * bounces the puck left or right
     * pre:  puck exists
     * post: xSpeed is flipped (puck reverses left/right direction)
     */
    public void bounceHorizontal() {
        xSpeed = -xSpeed;
    }

    /**
     * bounces the puck up or down
     * pre:  puck exists
     * post: ySpeed is flipped (puck reverses up/down direction)
     */
    public void bounceVertical() {
        ySpeed = -ySpeed;
    }

    /**
     * moves the puck by its current speed and applies light friction
     * pre:  puck exists
     * post: puck's position is updated, speed is capped, and speed never drops
     *       below the playable minimum while moving
     */
    public void update() {
        exactX = exactX + xSpeed;
        exactY = exactY + ySpeed;

        applyFriction();

        setX((int)Math.round(exactX));
        setY((int)Math.round(exactY));
        repaint();
    }

    /**
     * resets the puck after a goal
     * pre:  direction is either 1 or -1
     * post: puck is moved to (centerX, centerY) and served toward the given direction
     */
    public void reset(int centerX, int centerY, int direction, Random random) {
        setCenter(centerX, centerY);
        serve(direction, random);
        repaint();
    }

    /**
     * keeps exact coordinates synced after wall correction
     * pre:  x is a valid left edge for the puck
     * post: puck x position and exact x coordinate match
     */
    public void setPuckX(int x) {
        exactX = x;
        setX(x);
    }

    /**
     * keeps exact coordinates synced after wall correction
     * pre:  y is a valid top edge for the puck
     * post: puck y position and exact y coordinate match
     */
    public void setPuckY(int y) {
        exactY = y;
        setY(y);
    }

    /**
     * applies light friction to a moving puck
     * pre:  puck velocity has already been updated this frame
     * post: puck slows slightly, but not below minSpeed unless stopped
     */
    private void applyFriction() {
        double speed = getCurrentSpeed();
        if (speed <= minSpeed || speed == 0) {
            return;
        }

        xSpeed = xSpeed * FRICTION;
        ySpeed = ySpeed * FRICTION;

        if (getCurrentSpeed() < minSpeed) {
            setCurrentSpeed(minSpeed);
        }
    }

    /**
     * limits the puck if it is moving too fast
     * pre:  velocity may be any magnitude
     * post: velocity magnitude is no greater than maxSpeed
     */
    private void limitTopSpeed() {
        if (getCurrentSpeed() > maxSpeed) {
            setCurrentSpeed(maxSpeed);
        }
    }

    /**
     * gets current velocity magnitude
     * pre:  puck exists
     * post: returns the puck's speed in pixels per frame
     */
    private double getCurrentSpeed() {
        return Math.sqrt(xSpeed * xSpeed + ySpeed * ySpeed);
    }

    /**
     * changes velocity magnitude while preserving direction
     * pre:  speed is non-negative
     * post: puck moves at the requested speed unless it was stopped
     */
    private void setCurrentSpeed(double newSpeed) {
        double current = getCurrentSpeed();
        if (current == 0) {
            return;
        }

        double scale = newSpeed / current;
        xSpeed = xSpeed * scale;
        ySpeed = ySpeed * scale;
    }

    /**
     * leaves puck frame behavior to AirHockeyGame
     * pre:  none
     * post: nothing - movement is handled in AirHockeyGame, not here
     */
    public void act() {
    }

    /**
     * draws the puck
     * pre:  g is a valid Graphics object
     * post: draws the puck as a white circle with a dark grey middle so it kind of
     *       looks like a real hockey puck
     */
    public void paint(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillOval(0, 0, diameter, diameter);

        g.setColor(Color.DARK_GRAY);
        g.fillOval(3, 3, diameter - 6, diameter - 6);
    }
}
