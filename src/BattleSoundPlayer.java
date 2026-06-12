import java.io.File;
import java.util.concurrent.TimeUnit;

// plays the item-box sound during the candy battle
public class BattleSoundPlayer {

    private static final String SOUND_FILE = "assets/audio/item-box-camera-sound.mp3";

    private static Process currentSound = null;
    private static boolean keepPlaying = false;

    // starts looping the candy battle sound
    public static void startLoop() {
        stop();

        File soundFile = new File(SOUND_FILE);
        if (!soundFile.exists()) {
            return;
        }

        keepPlaying = true;

        Thread soundThread = new Thread(() -> {
            while (keepPlaying) {
                try {
                    currentSound = new ProcessBuilder("afplay", soundFile.getPath()).start();
                    currentSound.waitFor();
                } catch (Exception e) {
                    keepPlaying = false;
                }
            }
        });
        soundThread.setDaemon(true);
        soundThread.start();
    }

    // stops the candy battle sound
    public static void stop() {
        keepPlaying = false;
        if (currentSound != null) {
            currentSound.destroy();
            try {
                if (!currentSound.waitFor(200, TimeUnit.MILLISECONDS)) {
                    currentSound.destroyForcibly();
                }
            } catch (Exception e) {
                currentSound.destroyForcibly();
            }
            currentSound = null;
        }
    }
}
