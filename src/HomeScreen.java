import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

// the main menu - this is what you see before you start and after a match ends
public class HomeScreen extends JFrame {

    /**
     * builds the main menu window
     * pre:  none
     * post: the home screen window is built with a title, byline, and three buttons
     */
    public HomeScreen() {
        setTitle("Air Hockey");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                AirHockeyApp.quit();
            }
        });
        setSize(500, 400);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(20, 30, 48));

        JLabel title = new JLabel("AIR HOCKEY");
        title.setFont(new Font("SansSerif", Font.BOLD, 36));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel byline = new JLabel("by Brighton & Youngwoo");
        byline.setForeground(Color.LIGHT_GRAY);
        byline.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton playBtn = new JButton("PLAY");
        playBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        playBtn.addActionListener(e -> {
            MusicPlayer.lowerVolume();
            dispose();
            AirHockeyApp.launchGame();
        });

        JButton rulesBtn = new JButton("RULES");
        rulesBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        rulesBtn.addActionListener(e -> showRules());

        JButton historyBtn = new JButton("MATCH HISTORY");
        historyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        historyBtn.addActionListener(e -> showHistory());

        panel.add(Box.createVerticalGlue());
        panel.add(title);
        panel.add(byline);
        panel.add(Box.createVerticalStrut(30));
        panel.add(playBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(rulesBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(historyBtn);
        panel.add(Box.createVerticalGlue());

        setContentPane(panel);
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

            JScrollPane scroll = new JScrollPane(area);
            scroll.setPreferredSize(new Dimension(400, 300));

            JOptionPane.showMessageDialog(this, scroll,
                    "Match History", JOptionPane.PLAIN_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not read match history.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
