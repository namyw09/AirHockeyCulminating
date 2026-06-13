import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import framework.GameObject;

// the powerup token that shows up on the ice. we extend GameObject so we get
// collides() for free instead of writing our own collision math
public class Powerup extends GameObject {

    public static final int RADIUS        = 18;   // base radius for the 800x600 layout
    private int radius;                           // actual radius, scaled to the window
    public static final int FIELD_LIVE_MS = 5000; // ms the icon stays on the field
    public static final int EFFECT_MS     = 5000; // ms the paddle effect lasts after collection
    public static final int RESPAWN_MS    = 6000; // ms to wait before the next spawn

    // powerup types
    public static final int TYPE_SIZE  = 1; // makes owner paddle taller
    public static final int TYPE_SPEED = 2; // makes owner paddle faster
    public static final int TYPE_SLOW  = 3; // slows the other paddle

    private int  ownerPlayer; // 1 = left half (player 1), 2 = right half (player 2)
    private int  type;
    private long spawnTime;

    /**
     * creates a powerup token
     * pre:  cx and cy are valid center coordinates inside the rink,
     *       owner is 1 or 2, powerupType is TYPE_SIZE/TYPE_SPEED/TYPE_SLOW,
     *       spawnMillis is System.currentTimeMillis(), scale is positive
     * post: a new active, uncollected powerup is sized and positioned so its
     *       center is at (cx, cy), ready to be added to the game
     */
    public Powerup(int cx, int cy, int owner, int powerupType, long spawnMillis, double scale) {
        ownerPlayer = owner;
        type        = powerupType;
        spawnTime   = spawnMillis;
        radius      = Math.max(6, (int) Math.round(RADIUS * scale));

        // add 4px of padding on each side, otherwise the glow ring around the icon
        // gets cut off at the edges of the component (learned that the annoying way)
        setSize(radius * 2 + 8, radius * 2 + 8);
        setX(cx - radius - 4);
        setY(cy - radius - 4);
    }

    /**
     * gets the player who benefits from this powerup
     * pre:  powerup exists
     * post: returns 1 if this powerup benefits player 1, 2 if it benefits player 2
     */
    public int getOwnerPlayer() {
        return ownerPlayer;
    }

    /**
     * gets the powerup effect type
     * pre:  powerup exists
     * post: returns the type constant (TYPE_SIZE, TYPE_SPEED, or TYPE_SLOW)
     */
    public int getType() {
        return type;
    }

    /**
     * checks if the icon has been sitting around too long
     * pre:  nowMillis is the current time
     * post: returns true when the powerup should disappear
     */
    public boolean isExpired(long nowMillis) {
        return nowMillis - spawnTime >= FIELD_LIVE_MS;
    }

    /**
     * leaves powerup lifecycle behavior to AirHockeyGame
     * pre:  none
     * post: nothing - powerup has no per-frame behavior; lifecycle is managed by AirHockeyGame
     */
    public void act() {
    }

    /**
     * draws the powerup icon
     * pre:  g is a valid Graphics object
     * post: the powerup icon is drawn in the component's local coordinate space:
     *       gold "1.5x" for size, cyan arrows for speed, orange arrows for slow opponent
     */
    public void paint(Graphics g) {
        // center within the padded component (RADIUS + 4px glow padding each side)
        int cx = radius + 4;
        int cy = radius + 4;
        int r  = radius;

        // each powerup type gets its own color and label so players can tell them apart
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
            label     = "1.5x";
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
        g.setFont(new Font("SansSerif", Font.BOLD, 8));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, cx - fm.stringWidth(label) / 2, cy + fm.getAscent() / 2 - 2);
    }
}
