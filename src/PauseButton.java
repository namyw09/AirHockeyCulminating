import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import framework.GameObject;

// the little pause button chilling in the top-right corner of the window
public class PauseButton extends GameObject {

    private static final int WIDTH  = 100;
    private static final int HEIGHT = 30;

    private Runnable onClick;
    private boolean  hovered = false;

    /**
     * creates the pause button and click handler
     * pre:  onClick is not null
     * post: button is sized, positioned, and wired to call onClick when clicked
     */
    public PauseButton(int x, int y, Runnable onClick) {
        this.onClick = onClick;
        setSize(WIDTH, HEIGHT);
        setX(x);
        setY(y);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    /**
     * draws the pause button
     * pre:  g is a valid Graphics object
     * post: draws a rounded rectangle with "PAUSE" centered in it, and lightens the
     *       background when you hover so you can tell it's clickable
     */
    public void paint(Graphics g) {
        g.setColor(hovered ? new Color(60, 90, 140) : new Color(40, 58, 90));
        g.fillRoundRect(0, 0, WIDTH, HEIGHT, 8, 8);

        g.setColor(Color.WHITE);
        g.setFont(RetroFont.get(9f));
        java.awt.FontMetrics fm = g.getFontMetrics();
        String text = "|| PAUSE";
        int textX = (WIDTH - fm.stringWidth(text)) / 2;
        int textY = (HEIGHT + fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(text, textX, textY);
    }

    /**
     * leaves the pause button still each frame
     * pre:  none
     * post: nothing - the button has no per-frame behavior
     */
    public void act() {
    }
}
