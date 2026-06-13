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

        setSize(WIDTH, HEIGHT);
        setX(x);
        setY(y);
        // hand cursor
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // when this gets clicked GamePanel handles the actual pause menu
                onClick.run();
            }
        });
    }

    /**
     * draws the pause button
     */
    public void paint(Graphics g) {
        // draw the button background first, because text floating in space looks weird
        g.setColor(new Color(40, 58, 90));
        g.fillRoundRect(0, 0, WIDTH, HEIGHT, 8, 8);

        // small bold text heree!!
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 9));
        g.drawString("|| PAUSE", 28, 19);
    }

    public void act() {
    }
}
