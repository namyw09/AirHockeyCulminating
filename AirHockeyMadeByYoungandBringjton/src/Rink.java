import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import framework.GameObject;

// draws the rink - ice surface, goals, center line, scoreboard, and player labels
public class Rink extends GameObject {

    private int windowWidth;
    private int windowHeight;
    private int rinkX;
    private int rinkY;
    private int rinkWidth;
    private int rinkHeight;
    private int goalHeight;

    private String player1Name;
    private String player2Name;
    private int player1Score = 0;
    private int player2Score = 0;
    private String timeText = "01:30";

    // powerup icon state - set by AirHockeyGame, drawn in paint()
    private boolean showPowerup = false;
    private int powerupX    = 0;
    private int powerupY    = 0;
    private int powerupType = 0;

    /**
     * creates the rink background and saves the player labels
     * pre:  dimensions are positive and player names are not null
     * post: the rink covers the window and is ready to draw
     */
    public Rink(int ww, int wh, int rx, int ry, int rw, int rh, int gh, String p1, String p2) {
        windowWidth = ww;
        windowHeight = wh;
        rinkX = rx;
        rinkY = ry;
        rinkWidth = rw;
        rinkHeight = rh;
        goalHeight = gh;
        player1Name = p1;
        player2Name = p2;

        // sized to the full window so it acts as the background layer
        setSize(ww, wh);
        setX(0);
        setY(0);
    }

    /**
     * updates the score and timer shown at the top of the rink
     * pre:  the game gives the current scores and timer text
     * post: the new scoreboard values are saved and the rink repaints
     */
    public void setScoreboard(int p1Score, int p2Score, String timerText) {
        player1Score = p1Score;
        player2Score = p2Score;
        timeText = timerText;
        repaint();
    }

    /**
     * draws the rink, goals, labels, and scoreboard
     * pre:  the rink dimensions and scoreboard values are set
     * post: the current rink is drawn on screen
     */
    public void paint(Graphics g) {
        // dark background outside the rink
        g.setColor(new Color(20, 30, 48));
        g.fillRect(0, 0, windowWidth, windowHeight);

        // slightly darker border behind the ice
        g.setColor(new Color(10, 15, 25));
        g.fillRoundRect(rinkX - 10, rinkY - 10, rinkWidth + 20, rinkHeight + 20, 20, 20);

        // ice surface
        g.setColor(new Color(200, 225, 245));
        g.fillRoundRect(rinkX, rinkY, rinkWidth, rinkHeight, 16, 16);

        g.setColor(new Color(30, 60, 100));
        g.drawRoundRect(rinkX, rinkY, rinkWidth, rinkHeight, 16, 16);

        // goals on each side
        int goalY     = rinkY + (rinkHeight - goalHeight) / 2;
        int goalDepth = 18;

        g.setColor(new Color(25, 35, 50));
        g.fillRect(rinkX - goalDepth, goalY, goalDepth, goalHeight);
        g.fillRect(rinkX + rinkWidth, goalY, goalDepth, goalHeight);

        g.setColor(Color.WHITE);
        g.drawRect(rinkX - goalDepth, goalY, goalDepth, goalHeight);
        g.drawRect(rinkX + rinkWidth, goalY, goalDepth, goalHeight);

        // center dividing line
        int centerX = rinkX + rinkWidth / 2;
        g.setColor(new Color(100, 140, 180));
        g.drawLine(centerX, rinkY, centerX, rinkY + rinkHeight);

        if (showPowerup) {
            drawPowerupIcon(g, powerupX, powerupY);
        }

        drawTimer(g);
        drawPlayerScores(g);

        // control labels at the bottom of the header strip, one per side
        g.setColor(new Color(100, 150, 230));
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.drawString(player1Name + " (W/A/S/D)", rinkX + 10, rinkY - 6);

        g.setColor(new Color(220, 100, 100));
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        FontMetrics lm = g.getFontMetrics();
        g.drawString(player2Name + " (Arrows)",
                rinkX + rinkWidth - lm.stringWidth(player2Name + " (Arrows)") - 10, rinkY - 6);
    }

    /**
     * draws the retro timer: timeText in a monospaced font centered horizontally in the header,
     * surrounded by a white rectangle
     * pre:  timeText is not null; rinkY defines the available header height
     * post: a filled rectangle with a white border and retro monospaced time text is painted
     *       at the horizontal center of the window, vertically centered in the header strip
     */
    private void drawTimer(Graphics g) {
        g.setFont(new Font("Monospaced", Font.BOLD, 28));
        FontMetrics fm = g.getFontMetrics();
        int padX  = 14;
        int padY  = 7;
        int boxW  = fm.stringWidth(timeText) + padX * 2;
        int boxH  = fm.getAscent() + padY * 2;
        int boxX  = (windowWidth - boxW) / 2;
        int boxY  = (rinkY - boxH) / 2;

        // dark fill so the border reads clearly against the background
        g.setColor(new Color(10, 15, 25));
        g.fillRect(boxX, boxY, boxW, boxH);

        g.setColor(Color.WHITE);
        g.drawRect(boxX, boxY, boxW, boxH);
        g.drawString(timeText, boxX + padX, boxY + padY + fm.getAscent() - 2);
    }

    /**
     * draws each player's current score above their control label on their side of the header
     * pre:  player1Score and player2Score are set; rinkX, rinkY, rinkWidth are defined
     * post: player 1's score in blue is drawn above the left label; player 2's score in red is
     *       drawn above the right label, right-aligned to the label's right edge
     */
    private void drawPlayerScores(Graphics g) {
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        FontMetrics fm = g.getFontMetrics();
        int scoreY = rinkY - 24;

        // player 1 - left side, aligned with the label's left edge
        g.setColor(new Color(100, 150, 230));
        g.drawString(String.valueOf(player1Score), rinkX + 10, scoreY);

        // player 2 - right side, right-aligned to match the label's right edge
        g.setColor(new Color(220, 100, 100));
        String s2       = String.valueOf(player2Score);
        int labelRight  = rinkX + rinkWidth - 10;
        g.drawString(s2, labelRight - fm.stringWidth(s2), scoreY);
    }

    /**
     * shows a powerup icon at the given screen coordinates on the next repaint
     * pre:  x and y are valid center coordinates within the rink bounds;
     *       type is Powerup.TYPE_SIZE, TYPE_SPEED, or TYPE_SLOW
     * post: showPowerup is true and the correct icon will be drawn at (x, y)
     */
    public void setPowerupPosition(int x, int y, int type) {
        showPowerup = true;
        powerupX    = x;
        powerupY    = y;
        powerupType = type;
        repaint();
    }

    /**
     * hides the powerup icon on the next repaint
     * pre:  none
     * post: showPowerup is false and no icon will be drawn
     */
    public void clearPowerup() {
        showPowerup = false;
        repaint();
    }

    /**
     * draws the powerup icon at (cx, cy) using a color and label matched to its type
     * pre:  cx and cy are the center of the icon; powerupType is set to a valid type constant
     * post: a colored circle with a glow ring, white border, and type label is painted at (cx, cy)
     *       gold "2x" = size, cyan ">>" = speed, orange "<<" = slow opponent
     */
    private void drawPowerupIcon(Graphics g, int cx, int cy) {
        int r = Powerup.RADIUS;

        // pick color and label based on type
        Color fillColor;
        Color glowColor;
        String label;

        if (powerupType == Powerup.TYPE_SPEED) {
            fillColor = new Color(0, 200, 220);
            glowColor = new Color(0, 220, 255, 120);
            label     = ">>";
        } else if (powerupType == Powerup.TYPE_SLOW) {
            fillColor = new Color(255, 140, 0);
            glowColor = new Color(255, 160, 0, 120);
            label     = "<<";
        } else {
            // TYPE_SIZE (default)
            fillColor = new Color(255, 200, 0);
            glowColor = new Color(255, 220, 50, 120);
            label     = "2x";
        }

        // soft glow ring behind the icon
        g.setColor(glowColor);
        g.fillOval(cx - r - 4, cy - r - 4, (r + 4) * 2, (r + 4) * 2);

        // filled circle
        g.setColor(fillColor);
        g.fillOval(cx - r, cy - r, r * 2, r * 2);

        // white border
        g.setColor(Color.WHITE);
        g.drawOval(cx - r, cy - r, r * 2, r * 2);

        // label centered inside the circle
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, cx - fm.stringWidth(label) / 2, cy + fm.getAscent() / 2 - 2);
    }

    /**
     * the rink does not need to do anything each frame
     * post: no state changes
     */
    public void act() {
    }
}
