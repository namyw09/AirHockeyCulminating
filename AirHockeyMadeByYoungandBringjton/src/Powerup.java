import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import framework.GameObject;

// on-ice powerup token - extends GameObject so collides() works with the puck
public class Powerup extends GameObject {

    public static final int RADIUS        = 18;
    public static final int FIELD_LIVE_MS = 2000; // ms the icon stays on the field
    public static final int EFFECT_MS     = 5000; // ms the paddle effect lasts after collection
    public static final int RESPAWN_MS    = 6000; // ms to wait before the next spawn

    // powerup types
    public static final int TYPE_SIZE  = 1; // doubles owner paddle height
    public static final int TYPE_SPEED = 2; // doubles owner paddle speed
    public static final int TYPE_SLOW  = 3; // halves opponent paddle speed

    private int  ownerPlayer; // 1 = left half (player 1), 2 = right half (player 2)
    private int  type;
    private long spawnTime;
    private boolean active;
    private boolean collected;

    /**
     * pre:  cx and cy are valid center coordinates inside the rink,
     *       owner is 1 or 2, powerupType is TYPE_SIZE/TYPE_SPEED/TYPE_SLOW,
     *       spawnMillis is System.currentTimeMillis()
     * post: a new active, uncollected powerup is sized and positioned so its
     *       center is at (cx, cy), ready to be added to the game
     */
    public Powerup(int cx, int cy, int owner, int powerupType, long spawnMillis) {
        ownerPlayer = owner;
        type        = powerupType;
        spawnTime   = spawnMillis;
        active      = true;
        collected   = false;

        setSize(RADIUS * 2, RADIUS * 2);
        setX(cx - RADIUS);
        setY(cy - RADIUS);
    }

    /**
     * pre:  powerup exists
     * post: returns the horizontal center of the powerup icon on screen
     */
    public int getCenterX() {
        return getX() + RADIUS;
    }

    /**
     * pre:  powerup exists
     * post: returns the vertical center of the powerup icon on screen
     */
    public int getCenterY() {
        return getY() + RADIUS;
    }

    /**
     * pre:  powerup exists
     * post: returns 1 if this powerup benefits player 1, 2 if it benefits player 2
     */
    public int getOwnerPlayer() {
        return ownerPlayer;
    }

    /**
     * pre:  powerup exists
     * post: returns the type constant (TYPE_SIZE, TYPE_SPEED, or TYPE_SLOW)
     */
    public int getType() {
        return type;
    }

    /**
     * pre:  powerup exists
     * post: returns true if the icon is still on the field and has not been collected
     */
    public boolean isActive() {
        return active;
    }

    /**
     * pre:  powerup exists
     * post: returns true if the puck has already picked up this powerup
     */
    public boolean isCollected() {
        return collected;
    }

    /**
     * pre:  nowMillis is the current wall-clock time from System.currentTimeMillis()
     * post: if FIELD_LIVE_MS has passed since spawn and the powerup was not collected,
     *       active is set to false; otherwise no change
     */
    public void checkExpiry(long nowMillis) {
        if (active && !collected) {
            if (nowMillis - spawnTime >= FIELD_LIVE_MS) {
                active = false;
            }
        }
    }

    /**
     * pre:  powerup is active and on the field
     * post: collected and active are both set so the icon is removed from play
     */
    public void collect() {
        collected = true;
        active    = false;
    }

    // post: nothing - powerup has no per-frame behavior; lifecycle is managed by AirHockeyGame
    public void act() {
    }

    /**
     * pre:  g is a valid Graphics object
     * post: the powerup icon is drawn in the component's local coordinate space:
     *       gold "2x" for size, cyan ">>" for speed, orange "<<" for slow opponent
     */
    public void paint(Graphics g) {
        int cx = RADIUS;
        int cy = RADIUS;
        int r  = RADIUS;

        // pick fill color, glow color, and label based on type
        Color fillColor;
        Color glowColor;
        String label;

        if (type == TYPE_SPEED) {
            fillColor = new Color(0, 200, 220);
            glowColor = new Color(0, 220, 255, 120);
            label     = ">>";
        } else if (type == TYPE_SLOW) {
            fillColor = new Color(255, 140, 0);
            glowColor = new Color(255, 160, 0, 120);
            label     = "<<";
        } else {
            // TYPE_SIZE
            fillColor = new Color(255, 200, 0);
            glowColor = new Color(255, 220, 50, 120);
            label     = "2x";
        }

        // soft glow ring
        g.setColor(glowColor);
        g.fillOval(cx - r - 4, cy - r - 4, (r + 4) * 2, (r + 4) * 2);

        // filled circle
        g.setColor(fillColor);
        g.fillOval(cx - r, cy - r, r * 2, r * 2);

        // white border
        g.setColor(Color.WHITE);
        g.drawOval(cx - r, cy - r, r * 2, r * 2);

        // centered label
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, cx - fm.stringWidth(label) / 2, cy + fm.getAscent() / 2 - 2);
    }
}
