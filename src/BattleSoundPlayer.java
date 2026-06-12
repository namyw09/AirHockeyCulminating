import java.io.File;
import java.net.URISyntaxException;

// plays that extra item-box sound layer, but only while the candy battle is happening
public class BattleSoundPlayer {

    private static Process currentProcess = null;
    private static String soundFilePath = null;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> stop()));
    }

    /**
     * starts the item-box sound during the camera battle
     * pre:  item-box sound file exists
     * post: item-box sound loops until stop() is called
     */
    public static void startLoop() {
        stop();

        File soundFile = findItemBoxSoundFile();
        if (soundFile == null || !soundFile.exists()) {
            return;
        }

        final String path = soundFile.getAbsolutePath();
        soundFilePath = path;

        Thread soundThread = new Thread(() -> {
            try {
                do {
                    currentProcess = new ProcessBuilder("afplay", path).start();
                    currentProcess.waitFor();
                } while (path.equals(soundFilePath));
            } catch (Exception e) {
                // sound's not important enough to crash over, so just ignore any problems
            }
        });
        soundThread.setDaemon(true);
        soundThread.start();
    }

    /**
     * stops the item-box battle sound
     * pre:  none
     * post: battle sound stops immediately
     */
    public static void stop() {
        soundFilePath = null;
        if (currentProcess != null) {
            currentProcess.destroy();
            currentProcess = null;
        }
    }

    /**
     * finds the item-box camera sound file
     * pre:  none
     * post: returns the project audio file used during camera battles, or null
     *       if the location cannot be resolved
     */
    private static File findItemBoxSoundFile() {
        try {
            File binDir = new File(
                BattleSoundPlayer.class.getProtectionDomain()
                                       .getCodeSource()
                                       .getLocation()
                                       .toURI());
            return new File(binDir.getParentFile(), "assets/audio/item-box-camera-sound.mp3");
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
