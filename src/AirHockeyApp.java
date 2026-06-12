// Brighton Ng(p4), Youngwoo Nam(p5) ICS3U Culminating Project
// 2d AirHockey game
import java.awt.Font;

import javax.swing.UIManager;

public class AirHockeyApp {

    /**
     * starts the app at the home screen
     * pre:  none
     * post: the retro dialog theme is applied and the home screen is shown;
     *       the game launches when Play is pressed
     */
    public static void main(String[] args) {
        applyRetroTheme();
        showHome();
    }

    /**
     * applies the retro look to all pop-up dialogs
     * pre:  none
     * post: JOptionPane message text uses a readable Monospaced font while the
     *       dialog buttons use the Press Start 2P arcade font; if the theme
     *       cannot be applied the default look is kept
     */
    private static void applyRetroTheme() {
        try {
            // body text stays Monospaced so help/rules paragraphs read clearly
            UIManager.put("OptionPane.messageFont", new Font("Monospaced", Font.PLAIN, 13));
            // buttons get the pixel arcade font (short, ASCII-only labels)
            UIManager.put("OptionPane.buttonFont", RetroFont.get(11f));
        } catch (Exception e) {
            // theming is non-critical; fall back to the default dialog look
        }
    }

    /**
     * opens the home screen with menu music
     * pre:  none
     * post: a new HomeScreen is created, music plays, and the window is shown
     */
    public static void showHome() {
        HomeScreen home = new HomeScreen();
        MusicPlayer.startLoop(MusicPlayer.findThemeFile());
        home.setVisible(true);
    }

    /**
     * opens the home screen without starting music
     * pre:  none
     * post: a new HomeScreen is created without music (used when quitting mid-game)
     */
    public static void showHomeQuiet() {
        HomeScreen home = new HomeScreen();
        home.setVisible(true);
    }

    /**
     * creates and starts a new air hockey match
     * pre:  none
     * post: a new AirHockeyGame window is created, shown, and started
     */
    public static void launchGame() {
        AirHockeyGame game = new AirHockeyGame();
        game.setVisible(true);
        game.initComponents();
    }
}
