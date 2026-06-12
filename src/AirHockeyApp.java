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
     * makes all the popup dialogs match our retro vibe
     * pre:  none
     * post: the message text uses a normal Monospaced font but the buttons get
     *       the Press Start 2P arcade font. we found out the hard way the pixel
     *       font looks bad on long paragraphs, so only the buttons use it
     */
    private static void applyRetroTheme() {
        // keep the body text monospaced so the help/rules walls of text are actually readable
        UIManager.put("OptionPane.messageFont", new Font("Monospaced", Font.PLAIN, 13));
        // buttons get the cool pixel font - they're short and ASCII so it doesn't look weird
        UIManager.put("OptionPane.buttonFont", RetroFont.get(11f));
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
