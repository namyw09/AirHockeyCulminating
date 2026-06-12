import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

// the main menu - this is what you see before you start and after a match ends
public class HomeScreen extends JFrame {

    private static final int WIDTH  = 800;
    private static final int HEIGHT = 600;

    // plain dark menu background
    private static final Color BG_DARK = new Color(20, 30, 48);

    // kept as handles so they can be repositioned whenever the window resizes
    private JPanel  panel;
    private JLabel  title;
    private JLabel  byline;
    private JButton playBtn;
    private JButton rulesBtn;
    private JButton historyBtn;

    /**
     * builds the main menu window
     * pre:  none
     * post: the home screen window is fully built and ready to be shown;
     *       buttons are wired to their actions
     */
    public HomeScreen() {
        setTitle("Air Hockey");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // fill the whole screen, just like the game window
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setSize(WIDTH, HEIGHT);          // fallback size if the WM ignores maximize
        setLocationRelativeTo(null);

        panel = new JPanel() {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBackground(g);
            }
        };
        panel.setLayout(null);
        panel.setBackground(BG_DARK);

        // game title
        title = new JLabel("AIR HOCKEY", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        panel.add(title);

        // byline in a readable terminal font
        byline = new JLabel("by Brighton & Youngwoo", SwingConstants.CENTER);
        byline.setFont(new Font("Monospaced", Font.PLAIN, 13));
        byline.setForeground(new Color(120, 155, 200));
        panel.add(byline);

        playBtn = makeButton("PLAY", new Color(54, 124, 230));
        playBtn.addActionListener(e -> {
            MusicPlayer.lowerVolume();
            dispose();
            AirHockeyApp.launchGame();
        });
        panel.add(playBtn);

        rulesBtn = makeButton("RULES", new Color(40, 58, 90));
        rulesBtn.addActionListener(e -> showRules());
        panel.add(rulesBtn);

        historyBtn = makeButton("MATCH HISTORY", new Color(40, 58, 90));
        historyBtn.addActionListener(e -> showHistory());
        panel.add(historyBtn);

        // reposition everything whenever the window size changes (e.g. on maximize)
        panel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                layoutComponents();
            }
        });

        setContentPane(panel);
        layoutComponents();
    }

    /**
     * positions and sizes the title, byline, and buttons for the current window size
     * pre:  the panel and all menu components exist
     * post: every component is centered and scaled to fit the current window;
     *       falls back to the base 800x600 layout if the panel has no size yet
     */
    private void layoutComponents() {
        int w = panel.getWidth();
        int h = panel.getHeight();
        if (w <= 0 || h <= 0) {
            w = WIDTH;
            h = HEIGHT;
        }

        // scale text/buttons up on bigger screens, never below the original size
        float s = h / (float) HEIGHT;
        if (s < 1f) {
            s = 1f;
        }

        title.setFont(new Font("SansSerif", Font.BOLD, Math.round(40 * s)));
        title.setBounds(0, Math.round(h * 0.16f), w, Math.round(72 * s));

        byline.setFont(new Font("Monospaced", Font.PLAIN, Math.round(13 * s)));
        byline.setBounds(0, Math.round(h * 0.16f) + Math.round(80 * s), w, Math.round(28 * s));

        int btnW = Math.round(230 * s);
        int btnH = Math.round(54 * s);
        int gap  = Math.round(20 * s);
        int btnX = (w - btnW) / 2;
        int firstY = Math.round(h * 0.46f);

        playBtn.setFont(new Font("SansSerif", Font.BOLD, Math.round(14 * s)));
        playBtn.setBounds(btnX, firstY, btnW, btnH);

        rulesBtn.setFont(new Font("SansSerif", Font.BOLD, Math.round(14 * s)));
        rulesBtn.setBounds(btnX, firstY + (btnH + gap), btnW, btnH);

        historyBtn.setFont(new Font("SansSerif", Font.BOLD, Math.round(14 * s)));
        historyBtn.setBounds(btnX, firstY + 2 * (btnH + gap), btnW, btnH);

        panel.repaint();
    }

    /**
     * creates one styled menu button
     * pre:  text and bg are not null
     * post: returns a styled JButton with the game's dark theme and hover cursor
     */
    private JButton makeButton(String text, final Color bg) {
        final JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // light the button up when you hover over it so it feels clicky
        final Color hover = bg.brighter();
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hover);
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    /**
     * draws the menu background
     * pre:  g is a valid Graphics object
     * post: a decorative rink outline is drawn behind the menu content
     */
    private void drawBackground(Graphics g) {
        int w = panel.getWidth();
        int h = panel.getHeight();
        if (w <= 0 || h <= 0) {
            w = WIDTH;
            h = HEIGHT;
        }

        // a faint rink outline that fills most of the window as a subtle backdrop
        int marginX = w / 12;
        int marginY = h / 12;
        int rw = w - marginX * 2;
        int rh = h - marginY * 2;

        g.setColor(new Color(28, 42, 65));
        g.fillRoundRect(marginX, marginY, rw, rh, 24, 24);

        g.setColor(new Color(35, 55, 85));
        g.drawRoundRect(marginX, marginY, rw, rh, 24, 24);

        // center line
        g.drawLine(w / 2, marginY, w / 2, marginY + rh);

        // center circle
        int cr = Math.min(rw, rh) / 8;
        g.drawOval(w / 2 - cr, marginY + rh / 2 - cr, cr * 2, cr * 2);
    }

    /**
     * shows the rules dialog
     * pre:  none
     * post: a dialog showing the game controls and rules is displayed
     */
    private void showRules() {
        JOptionPane.showMessageDialog(this,
                "CONTROLS\n"
                + "  Player 1 (Blue):  W / A / S / D\n"
                + "  Player 2 (Red):   Arrow Keys\n\n"
                + "HOW TO WIN\n"
                + "  Hit the puck into the opponent's goal.\n"
                + "  First to 7 goals wins!\n\n"
                + "POWER-UPS  (appear every 6 seconds, last 5 seconds)\n"
                + "  Gold  1.5x  — your paddle grows 1.5x taller for 5s\n"
                + "  Cyan  >>    — your paddle moves 1.5x faster for 5s\n"
                + "  Orange <<   — opponent's paddle slows to 0.75x for 5s\n\n"
                + "  Collect by moving your paddle over the icon.",
                "Rules", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * shows saved match history
     * pre:  none
     * post: reads match_history.txt and displays all past results in a scrollable dialog;
     *       shows a message if no matches have been played yet
     */
    private void showHistory() {
        File file = new File("match_history.txt");

        if (!file.exists()) {
            JOptionPane.showMessageDialog(this,
                    "No matches played yet.",
                    "Match History", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();

            JTextArea area = new JTextArea(sb.toString());
            area.setEditable(false);
            area.setFont(new Font("Monospaced", Font.PLAIN, 13));
            area.setBackground(new Color(20, 30, 48));
            area.setForeground(Color.WHITE);
            area.setMargin(new java.awt.Insets(6, 10, 6, 10));

            JScrollPane scroll = new JScrollPane(area);
            scroll.setPreferredSize(new Dimension(520, 300));

            JOptionPane.showMessageDialog(this, scroll,
                    "Match History", JOptionPane.PLAIN_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not read match history.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
