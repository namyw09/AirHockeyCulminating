import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

// the main menu - this is what you see before you start and after a match ends
public class HomeScreen extends JFrame {

    /**
     * builds the main menu window
     * pre:  none
     * post: the home screen window is built with a title, byline, and three buttons
     */
    public HomeScreen() {
        setTitle("Air Hockey");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 1, 0, 10));
        panel.setBackground(new Color(20, 30, 48));
        panel.setBorder(BorderFactory.createEmptyBorder(45, 80, 45, 80));

        JLabel title = new JLabel("AIR HOCKEY");
        title.setFont(new Font("SansSerif", Font.BOLD, 36));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(JLabel.CENTER);

        JLabel byline = new JLabel("by Brighton & Youngwoo");
        byline.setForeground(Color.LIGHT_GRAY);
        byline.setHorizontalAlignment(JLabel.CENTER);

        JButton playBtn = makeButton("PLAY", () -> {
            MusicPlayer.lowerVolume();
            dispose();
            AirHockeyApp.launchGame();
        });

        JButton rulesBtn = makeButton("RULES", () -> showRules());
        JButton historyBtn = makeButton("MATCH HISTORY", () -> showHistory());

        panel.add(title);
        panel.add(byline);
        panel.add(playBtn);
        panel.add(rulesBtn);
        panel.add(historyBtn);

        setContentPane(panel);
    }

    /**
     * makes one centered menu button
     * pre:  text is not empty and action is not null
     * post: returns a button that runs action when clicked
     */
    private JButton makeButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(e -> action.run());
        return button;
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
            Scanner reader = new Scanner(file);

            while (reader.hasNextLine()) {
                sb.append(reader.nextLine()).append("\n");
            }
            reader.close();

            JOptionPane.showMessageDialog(this, sb.toString(),
                    "Match History", JOptionPane.PLAIN_MESSAGE);

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not read match history.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
