import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

import framework.GameObject;

// the puck - moves around and bounces off walls and paddles
public class Puck extends GameObject {

    private static final int RADIUS   = 14;
    private static final int DIAMETER = RADIUS * 2;
    private static final double START_SPEED = 10.5;
    private static final double MIN_SPEED   = 5.5;
    private static final double MAX_SPEED   = 16.0;
    private static final double HIT_BOOST   = 1.08;
    private static final double FRICTION    = 0.996;

    private double exactX;
    private double exactY;
    private double xSpeed = 0;
    private double ySpeed = 0;

    /**
     * creates a puck at a center point
     * pre:  centerX and centerY are valid positions inside the rink
     * post: puck is created and placed so its center is at (centerX, centerY)
     */
    public Puck(int centerX, int centerY) {
        setSize(DIAMETER, DIAMETER);
        setCenter(centerX, centerY);
    }

    /**
     * gets the puck center x position
     * pre:  puck exists on screen
     * post: returns the x coordinate of the center of the puck
     */
    public int getCenterX() {
        return getX() + RADIUS;
    }

    /**
     * gets the puck center y position
     * pre:  puck exists on screen
     * post: returns the y coordinate of the center of the puck
     */
    public int getCenterY() {
        return getY() + RADIUS;
    }

    /**
     * gets the puck radius
     * pre:  puck exists
     * post: returns the radius of the puck in pixels
     */
    public int getRadius() {
        return RADIUS;
    }

    /**
     * sets the puck center position
     * pre:  centerX and centerY are valid positions inside the rink
     * post: puck is moved so its center is at (centerX, centerY)
     */
    private void setCenter(int centerX, int centerY) {
        exactX = centerX - RADIUS;
        exactY = centerY - RADIUS;
        setX((int)Math.round(exactX));
        setY((int)Math.round(exactY));
    }

    /**
     * serves the puck in a randomized diagonal direction
     * pre:  direction is either 1 or -1, random is not null
     * post: puck velocity is set to a playable diagonal serve toward direction
     */
    public void serve(int direction, Random random) {
        double vertical = 0.30 + random.nextDouble() * 0.45;
        if (random.nextBoolean()) {
            vertical = -vertical;
        }

        xSpeed = START_SPEED * direction;
        ySpeed = START_SPEED * vertical;
        clampSpeed();
    }

    /**
     * bounces the puck off a paddle and adds a small speed boost
     * pre:  puck is overlapping the given paddle
     * post: xSpeed is aimed away from the paddle, speed increases slightly,
     *       and puck is pushed outside the paddle so it doesn't get stuck
     */
    public void hitByPaddle(Paddle paddle) {
        double speedBeforeHit = getSpeed();

        if (paddle.getX() + paddle.getWidth() / 2 < getCenterX()) {
            xSpeed = Math.abs(xSpeed);
        } else {
            xSpeed = -Math.abs(xSpeed);
        }

        if (speedBeforeHit > 0 && Math.abs(xSpeed) < MIN_SPEED) {
            xSpeed = (xSpeed < 0) ? -MIN_SPEED : MIN_SPEED;
        }

        multiplySpeed(HIT_BOOST);

        if (xSpeed > 0) {
            exactX = paddle.getX() + paddle.getWidth() + 1;
        } else {
            exactX = paddle.getX() - DIAMETER - 1;
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
     * post: puck slows slightly, but not below MIN_SPEED unless stopped
     */
    private void applyFriction() {
        double speed = getSpeed();
        if (speed <= MIN_SPEED || speed == 0) {
            return;
        }

        multiplySpeed(FRICTION);
        if (getSpeed() < MIN_SPEED) {
            setSpeed(MIN_SPEED);
        }
    }

    /**
     * multiplies the current velocity
     * pre:  factor is positive
     * post: velocity magnitude is multiplied and capped at MAX_SPEED
     */
    private void multiplySpeed(double factor) {
        xSpeed = xSpeed * factor;
        ySpeed = ySpeed * factor;
        clampSpeed();
    }

    /**
     * caps the current velocity
     * pre:  velocity may be any magnitude
     * post: velocity magnitude is no greater than MAX_SPEED
     */
    private void clampSpeed() {
        if (getSpeed() > MAX_SPEED) {
            setSpeed(MAX_SPEED);
        }
    }

    /**
     * gets current velocity magnitude
     * pre:  puck exists
     * post: returns the puck's speed in pixels per frame
     */
    private double getSpeed() {
        return Math.sqrt(xSpeed * xSpeed + ySpeed * ySpeed);
    }

    /**
     * changes velocity magnitude while preserving direction
     * pre:  speed is non-negative
     * post: puck moves at the requested speed unless it was stopped
     */
    private void setSpeed(double speed) {
        double current = getSpeed();
        if (current == 0) {
            return;
        }

        double scale = speed / current;
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
     * post: puck is drawn as a white circle with a dark grey fill inside
     */
    public void paint(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillOval(0, 0, DIAMETER, DIAMETER);

        g.setColor(Color.DARK_GRAY);
        g.fillOval(3, 3, DIAMETER - 6, DIAMETER - 6);
    }
}
