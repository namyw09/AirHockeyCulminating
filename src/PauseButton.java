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

    /**
     * creates the pause button and click handler
     * pre:  onClick is not null
     * post: button is sized, positioned, and wired to call onClick when clicked
     */
    public PauseButton(int x, int y, Runnable onClick) {
        this.onClick = onClick;
        // keep the button size in one spot so it is easy to move around later
        setSize(WIDTH, HEIGHT);
        setX(x);
        setY(y);
        // hand cursor makes it feel like an actual button and not random wall art
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // when this gets clicked, GamePanel handles the actual pause menu
                onClick.run();
            }
        });
    }

    /**
     * draws the pause button
     * pre:  none
     * post: draws a simple pause button
     */
    public void paint(Graphics g) {
        // draw the button background first, because text floating in space looks cursed
        g.setColor(new Color(40, 58, 90));
        g.fillRoundRect(0, 0, WIDTH, HEIGHT, 8, 8);

        // small bold text is enough here, no need to make it dramatic
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 9));
        g.drawString("|| PAUSE", 28, 19);
    }

    /**
     * leaves the pause button still each frame
     * pre:  none
     * post: nothing - the button has no per-frame behavior
     */
    public void act() {
        // nothing to update; the pause button is just vibing until someone clicks it
    }
}
