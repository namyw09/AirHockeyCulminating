// Brighton Ng(p4), Youngwoo Nam(p5) ICS3U Culminating Project
// 2d AirHockey game
public class AirHockeyApp {

    /**
     * starts the app at the home screen
     * pre:  none
     * post: the home screen is shown; the game launches when Play is pressed
     */
    public static void main(String[] args) {
        showHome();
    }

    /**
     * opens the home screen with menu music
     * pre:  none
     * post: a new HomeScreen is created, music plays, and the window is shown
     */
    public static void showHome() {
        HomeScreen home = new HomeScreen();
        MusicPlayer.start(MusicPlayer.findThemeFile());
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
