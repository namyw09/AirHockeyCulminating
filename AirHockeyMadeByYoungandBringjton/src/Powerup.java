// data class for a single on-ice powerup - no Swing, no act(), just state
public class Powerup {

    public static final int RADIUS        = 18;
    public static final int FIELD_LIVE_MS = 2000; // ms the icon stays on the field
    public static final int EFFECT_MS     = 5000; // ms the paddle effect lasts after collection
    public static final int RESPAWN_MS    = 6000; // ms to wait before the next spawn

    private int  centerX;
    private int  centerY;
    private int  ownerPlayer;  // 1 = left half (player 1), 2 = right half (player 2)
    private long spawnTime;
    private boolean active;
    private boolean collected;

    /**
     * pre:  cx and cy are valid center coordinates inside the rink,
     *       owner is 1 or 2, spawnMillis is System.currentTimeMillis()
     * post: a new active, uncollected powerup exists at (cx, cy) for the given owner
     */
    public Powerup(int cx, int cy, int owner, long spawnMillis) {
        centerX     = cx;
        centerY     = cy;
        ownerPlayer = owner;
        spawnTime   = spawnMillis;
        active      = true;
        collected   = false;
    }

    /**
     * pre:  powerup exists
     * post: returns the horizontal center of the powerup icon on screen
     */
    public int getCenterX() {
        return centerX;
    }

    /**
     * pre:  powerup exists
     * post: returns the vertical center of the powerup icon on screen
     */
    public int getCenterY() {
        return centerY;
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
}
