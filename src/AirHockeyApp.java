// Brighton Ng(p4), Youngwoo Nam(p5) ICS3U Culminating Project
// 2d AirHockey game
// This still has the same job as the milestone AirHockeyApp:
// start the program. The final version also starts menu music and cleans it up.
public class AirHockeyApp {

    /**
     * starts the app at the start screen
     * pre:  none
     * post: the start screen is shown and sound is stopped when the app closes
     */
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            MusicPlayer.stop();
        }));
        showHome();
    }

    /**
     * opens the start screen with menu music
     * pre:  none
     * post: a new StartPanel is created, the menu music starts playing, and the window is shown
     */
    public static void showHome() {
        StartPanel home = new StartPanel();
        MusicPlayer.startLoop(MusicPlayer.findThemeFile());
        home.setVisible(true);
    }

    /**
     * creates and starts a new air hockey match
     * pre:  none
     * post: a new GamePanel window is created, shown, and started
     */
    public static void launchGame() {
        GamePanel game = new GamePanel();
        game.setVisible(true);
        game.initComponents();
    }

    /**
     * closes the app after stopping any background sound
     * pre:  none
     * post: music is stopped and the program exits
     */
    public static void quit() {
        MusicPlayer.stop();
        System.exit(0);
    }
}
