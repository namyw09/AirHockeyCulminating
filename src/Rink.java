import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import framework.GameObject;

// draws basically everything in the background - the ice, the goals, the center line,
// the scoreboard up top, and the player name labels
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
    private String timeText = "01:00";
    private String centerMessage = "";

    // retro atmosphere flags driven by the game in the final seconds / overtime
    private boolean finalCountdown = false;  // last 5 seconds of regulation
    private boolean suddenDeath    = false;  // overtime: next goal wins


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

        // make it the size of the whole window so it works as the background layer
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
     * sets the large center message shown over the rink
     * pre:  message is not null
     * post: centerMessage is updated and the rink repaints
     */
    public void setCenterMessage(String message) {
        centerMessage = message;
        repaint();
    }

    /**
     * turns the final-seconds intensity treatment on or off
     * pre:  none
     * post: when on, the rink border pulses red and the timer turns red
     */
    public void setFinalCountdown(boolean on) {
        finalCountdown = on;
        repaint();
    }

    /**
     * turns the sudden-death overtime treatment on or off
     * pre:  none
     * post: when on, a pulsing red border and a "SUDDEN DEATH" banner are shown
     */
    public void setSuddenDeath(boolean on) {
        suddenDeath = on;
        repaint();
    }

    /**
     * draws the rink, goals, labels, and scoreboard
     * pre:  the rink dimensions and scoreboard values are set
     * post: the current rink is drawn on screen
     */
    public void paint(Graphics g) {
        // fill the whole window with the dark background first
        g.setColor(new Color(20, 30, 48));
        g.fillRect(0, 0, windowWidth, windowHeight);

        // a slightly darker rounded rectangle behind the ice, gives it a little border
        g.setColor(new Color(10, 15, 25));
        g.fillRoundRect(rinkX - 10, rinkY - 10, rinkWidth + 20, rinkHeight + 20, 20, 20);

        // the actual ice
        g.setColor(new Color(200, 225, 245));
        g.fillRoundRect(rinkX, rinkY, rinkWidth, rinkHeight, 16, 16);

        g.setColor(new Color(30, 60, 100));
        g.drawRoundRect(rinkX, rinkY, rinkWidth, rinkHeight, 16, 16);

        // the goal nets, one on each side
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

        // pulsing red border / banner during the final seconds and overtime
        drawIntensity(g);

        drawTimer(g);
        drawPlayerScores(g);
        drawCenterMessage(g);

        // control labels at the bottom of the header strip, one per side
        g.setColor(new Color(100, 150, 230));
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g.drawString(player1Name + " (W/A/S/D)", rinkX + 10, rinkY - 6);

        g.setColor(new Color(220, 100, 100));
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        FontMetrics lm = g.getFontMetrics();
        g.drawString(player2Name + " (Arrows)",
                rinkX + rinkWidth - lm.stringWidth(player2Name + " (Arrows)") - 10, rinkY - 6);
    }

    /**
     * draws a large countdown or start message over the middle of the rink
     * pre:  centerMessage is set
     * post: if centerMessage is not empty, it is drawn above the center puck
     */
    private void drawCenterMessage(Graphics g) {
        if (centerMessage == null || centerMessage.length() == 0) {
            return;
        }

        g.setFont(RetroFont.get(52f));
        FontMetrics fm = g.getFontMetrics();
        int x = rinkX + rinkWidth / 2 - fm.stringWidth(centerMessage) / 2;
        int y = rinkY + rinkHeight / 2 - 95;

        g.setColor(new Color(10, 15, 25, 170));
        g.fillRoundRect(x - 28, y - fm.getAscent() - 18,
                fm.stringWidth(centerMessage) + 56, fm.getAscent() + 36, 18, 18);

        // the countdown numbers glow red; normal messages (GO!, etc.) stay white
        g.setColor(finalCountdown ? new Color(255, 80, 80) : Color.WHITE);
        g.drawString(centerMessage, x, y);
    }

    /**
     * draws the final-seconds and sudden-death intensity overlay
     * pre:  finalCountdown / suddenDeath flags reflect the game state
     * post: when either is on, a red border pulses around the rink; in overtime
     *       a "SUDDEN DEATH" banner is drawn under the timer
     */
    private void drawIntensity(Graphics g) {
        if (!finalCountdown && !suddenDeath) {
            return;
        }

        // use a sine wave to make the border kind of "breathe" in and out about twice a
        // second. took some trial and error to get the timing to not look seizure-y
        double phase = (System.currentTimeMillis() % 700) / 700.0;
        float  pulse = (float) (0.35 + 0.65 * Math.abs(Math.sin(phase * Math.PI)));
        int    alpha = (int) (pulse * 200);

        g.setColor(new Color(255, 40, 40, alpha));
        // draw a few rectangles inside each other so it looks like one thick glowy border
        for (int i = 0; i < 4; i++) {
            g.drawRoundRect(rinkX - 6 - i, rinkY - 6 - i,
                    rinkWidth + 12 + i * 2, rinkHeight + 12 + i * 2, 18, 18);
        }

        if (suddenDeath) {
            String banner = "SUDDEN DEATH";
            g.setFont(RetroFont.get(16f));
            FontMetrics fm = g.getFontMetrics();
            int bx = rinkX + rinkWidth / 2 - fm.stringWidth(banner) / 2;
            int by = rinkY + rinkHeight + 30;

            g.setColor(new Color(10, 15, 25, 200));
            g.fillRoundRect(bx - 12, by - fm.getAscent() - 6,
                    fm.stringWidth(banner) + 24, fm.getAscent() + 12, 10, 10);
            g.setColor(new Color(255, 60, 60, alpha));
            g.drawString(banner, bx, by);
        }
    }

    /**
     * draws the retro timer: timeText in a monospaced font centered horizontally in the header,
     * surrounded by a white rectangle
     * pre:  timeText is not null; rinkY defines the available header height
     * post: a filled rectangle with a white border and retro monospaced time text is painted
     *       at the horizontal center of the window, vertically centered in the header strip
     */
    private void drawTimer(Graphics g) {
        g.setFont(RetroFont.get(20f));
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

        // the timer flashes red in the final seconds for urgency
        Color timerColor = finalCountdown ? new Color(255, 80, 80) : Color.WHITE;
        g.setColor(timerColor);
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
        g.setFont(RetroFont.get(18f));
        FontMetrics fm = g.getFontMetrics();
        int scoreY = rinkY - 22;

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
     * the rink does not need to do anything each frame
     * pre:  none
     * post: no state changes
     */
    public void act() {
    }
}
