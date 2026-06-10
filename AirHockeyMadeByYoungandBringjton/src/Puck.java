import java.awt.Color;
import java.awt.Graphics;

import framework.GameObject;

// the puck - moves around and bounces off walls and paddles
public class Puck extends GameObject {

    private static final int   RADIUS   = 14;
    private static final int   DIAMETER = RADIUS * 2;
    private static final float SPEED    = 9f;
    private static final float FRICTION = 0.999f; // applied every frame; slows puck very gradually
    private static final float MAX_SPEED = 18f;   // cap so a hard paddle hit stays playable

    float xSpeed = SPEED;
    float ySpeed = 3f;

    // pre:  centerX and centerY are valid positions inside the rink
    // post: puck is created and placed so its center is at (centerX, centerY)
    public Puck(int centerX, int centerY) {
        setSize(DIAMETER, DIAMETER);
        setX(centerX - RADIUS);
        setY(centerY - RADIUS);
    }

    // pre:  puck exists on screen
    // post: returns the x coordinate of the center of the puck
    public int getCenterX() {
        return getX() + RADIUS;
    }

    // pre:  puck exists on screen
    // post: returns the y coordinate of the center of the puck
    public int getCenterY() {
        return getY() + RADIUS;
    }

    // post: returns the radius of the puck in pixels
    public int getRadius() {
        return RADIUS;
    }

    // post: returns current horizontal speed (for tests)
    float getXSpeed() {
        return xSpeed;
    }

    // post: returns current vertical speed (for tests)
    float getYSpeed() {
        return ySpeed;
    }

    /**
     * pre:  puck is overlapping the given paddle
     * post: xSpeed is reversed and adjusted by half the paddle's velocity;
     *       combined speed is clamped to MAX_SPEED; puck is pushed outside the paddle
     */
    public void hitByPaddle(Paddle paddle) {
        xSpeed = -xSpeed + paddle.getVelocityX() * 0.5f;
        ySpeed =  ySpeed + paddle.getVelocityY() * 0.5f;

        // clamp to MAX_SPEED while preserving direction
        if (Math.abs(xSpeed) > MAX_SPEED) {
            xSpeed = MAX_SPEED * Math.signum(xSpeed);
        }
        if (Math.abs(ySpeed) > MAX_SPEED) {
            ySpeed = MAX_SPEED * Math.signum(ySpeed);
        }

        if (xSpeed > 0) {
            setX(paddle.getX() + paddle.getWidth() + 1);
        } else {
            setX(paddle.getX() - DIAMETER - 1);
        }

        repaint();
    }

    // post: xSpeed is flipped (puck reverses left/right direction)
    public void bounceHorizontal() {
        xSpeed = -xSpeed;
    }

    // post: ySpeed is flipped (puck reverses up/down direction)
    public void bounceVertical() {
        ySpeed = -ySpeed;
    }

    /**
     * pre:  none
     * post: puck position advances by its current velocity; friction is applied so
     *       speed decays slightly each frame after a hard hit
     */
    public void update() {
        xSpeed *= FRICTION;
        ySpeed *= FRICTION;
        setX(getX() + (int) Math.round(xSpeed));
        setY(getY() + (int) Math.round(ySpeed));
        repaint();
    }

    // pre:  direction is either 1 or -1
    // post: puck is moved to (centerX, centerY) and speed is reset toward the given direction
    public void reset(int centerX, int centerY, int direction) {
        setX(centerX - RADIUS);
        setY(centerY - RADIUS);
        xSpeed = SPEED * direction;
        ySpeed = 3f;
        repaint();
    }

    // post: nothing - movement is handled in AirHockeyGame, not here
    public void act() {
    }

    // pre:  g is a valid Graphics object
    // post: puck is drawn as a white circle with a dark grey fill inside
    public void paint(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillOval(0, 0, DIAMETER, DIAMETER);

        g.setColor(Color.DARK_GRAY);
        g.fillOval(3, 3, DIAMETER - 6, DIAMETER - 6);
    }
}
