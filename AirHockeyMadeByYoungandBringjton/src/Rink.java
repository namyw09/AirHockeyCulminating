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

        // center the scoreboard so longer names still look okay
        String scoreboard = player1Name + " " + player1Score
                + "   " + timeText + "   "
                + player2Name + " " + player2Score;
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(scoreboard, (windowWidth - metrics.stringWidth(scoreboard)) / 2, 50);

        // control labels above each side of the rink
        g.setColor(new Color(100, 150, 230));
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.drawString(player1Name + " (W/A/S/D)", rinkX + 10, rinkY - 10);

        g.setColor(new Color(220, 100, 100));
        g.drawString(player2Name + " (Arrows)", rinkX + rinkWidth - 120, rinkY - 10);
    }

    /**
     * the rink does not need to do anything each frame
     * post: no state changes
     */
    public void act() {
    }
}
