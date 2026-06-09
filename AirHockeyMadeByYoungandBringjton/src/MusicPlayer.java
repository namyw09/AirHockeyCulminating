import java.io.File;
import java.net.URISyntaxException;

// plays background music via macOS afplay; loops until stop() is called
public class MusicPlayer {

    private static Process  currentProcess = null;
    private static Thread   loopThread     = null;
    private static volatile boolean playing = false;

    /**
     * pre:  none
     * post: background music starts looping from the given file; any previously
     *       playing music is stopped first
     */
    public static void start(File musicFile) {
        stop();
        if (!musicFile.exists()) {
            return;
        }

        playing = true;
        final String path = musicFile.getAbsolutePath();

        loopThread = new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder("afplay", path);
                currentProcess = pb.start();
                currentProcess.waitFor();
            } catch (Exception e) {
                // ignore
            }
        });
        loopThread.setDaemon(true);
        loopThread.start();
    }

    /**
     * pre:  none
     * post: music playback is stopped and the afplay process is killed
     */
    public static void stop() {
        playing = false;
        if (currentProcess != null) {
            currentProcess.destroy();
            currentProcess = null;
        }
    }

    /**
     * pre:  none
     * post: returns the theme.mp3 file located next to the bin directory,
     *       or null if the location cannot be resolved
     */
    public static File findThemeFile() {
        try {
            File binDir = new File(
                MusicPlayer.class.getProtectionDomain()
                                 .getCodeSource()
                                 .getLocation()
                                 .toURI());
            return new File(binDir.getParentFile(), "theme.mp3");
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
