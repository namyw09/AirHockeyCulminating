// Brighton Ng(p4), Youngwoo Nam(p5) ICS3U Culminating Project
// 2d AirHockey game
import java.awt.Font;

import javax.swing.UIManager;

public class AirHockeyApp {

    /**
     * starts the app at the home screen
     * pre:  none
     * post: the app theme is applied and the home screen is shown
     *       the game launches when Play is pressed
     */
    public static void main(String[] args) {
        applyAppTheme();
        showHome();
    }

    /**
     * makes all the popup dialogs match the app
     * pre:  none
     * post: message text and buttons use regular built-in Java fonts
     */
    private static void applyAppTheme() {
        UIManager.put("OptionPane.messageFont", new Font("Monospaced", Font.PLAIN, 13));
        UIManager.put("OptionPane.buttonFont", new Font("SansSerif", Font.BOLD, 12));
    }

    /**
     * opens the home screen with menu music
     * pre:  none
     * post: a new HomeScreen is created, the menu music starts playing, and the window is shown
     */
    public static void showHome() {
        HomeScreen home = new HomeScreen();
        MusicPlayer.startLoop(MusicPlayer.findThemeFile());
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
