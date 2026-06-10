import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
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

// main menu shown before and after each match
public class HomeScreen extends JFrame {

    private static final int WIDTH  = 800;
    private static final int HEIGHT = 600;

    /**
     * pre:  none
     * post: the home screen window is fully built and ready to be shown;
     *       buttons are wired to their actions
     */
    public HomeScreen() {
        setTitle("Air Hockey");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel() {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBackground(g);
            }
        };
        panel.setLayout(null);
        panel.setBackground(new Color(20, 30, 48));

        // game title
        JLabel title = new JLabel("AIR HOCKEY", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 58));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 110, WIDTH, 72);
        panel.add(title);

        // byline
        JLabel byline = new JLabel("by Brighton & Youngwoo", SwingConstants.CENTER);
        byline.setFont(new Font("SansSerif", Font.PLAIN, 15));
        byline.setForeground(new Color(120, 155, 200));
        byline.setBounds(0, 188, WIDTH, 28);
        panel.add(byline);

        // buttons centered horizontally
        int btnX = (WIDTH - 230) / 2;

        JButton playBtn = makeButton("▶   PLAY", new Color(54, 124, 230));
        playBtn.setBounds(btnX, 268, 230, 54);
        playBtn.addActionListener(e -> {
            dispose();
            AirHockeyApp.launchGame();
        });
        panel.add(playBtn);

        JButton rulesBtn = makeButton("RULES", new Color(40, 58, 90));
        rulesBtn.setBounds(btnX, 342, 230, 54);
        rulesBtn.addActionListener(e -> showRules());
        panel.add(rulesBtn);

        JButton historyBtn = makeButton("MATCH HISTORY", new Color(40, 58, 90));
        historyBtn.setBounds(btnX, 416, 230, 54);
        historyBtn.addActionListener(e -> showHistory());
        panel.add(historyBtn);

        setContentPane(panel);
    }

    /**
     * pre:  text and bg are not null
     * post: returns a styled JButton with the game's dark theme and hover cursor
     */
    private JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 17));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * pre:  g is a valid Graphics object
     * post: a decorative rink outline is drawn behind the menu content
     */
    private void drawBackground(Graphics g) {
        // faint rink outline as decoration
        g.setColor(new Color(28, 42, 65));
        g.fillRoundRect(60, 60, 680, 480, 20, 20);

        g.setColor(new Color(35, 55, 85));
        g.drawRoundRect(60, 60, 680, 480, 20, 20);

        // center line
        g.drawLine(WIDTH / 2, 60, WIDTH / 2, 540);

        // center circle
        g.drawOval(WIDTH / 2 - 50, HEIGHT / 2 - 50, 100, 100);
    }

    /**
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
                + "  Collect by moving your paddle or puck over the icon.",
                "Rules", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
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
