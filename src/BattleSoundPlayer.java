import java.io.File;

// plays the item-box sound on a loop during the candy battle
public class BattleSoundPlayer {

    private static final String SOUND_FILE = "assets/audio/item-box-camera-sound.mp3";

    private static Process soundProcess = null;
    private static boolean playing = false;

    /**
     * pre:  the item-box sound file exists in assets/audio
     * post: the item-box sound starts looping in the background until stop() is called
     */
    public static void startLoop() {
        stop();

        File soundFile = new File(SOUND_FILE);
        if (!soundFile.exists()) {
            return;
        }

        playing = true;

        Thread soundThread = new Thread(() -> {
            while (playing) {
                try {
                    soundProcess = new ProcessBuilder("afplay", soundFile.getPath()).start();
                    soundProcess.waitFor();
                } catch (Exception e) {
                    // sound's not important enough to crash over, so just stop trying
                    playing = false;
                }
            }
        });
        soundThread.setDaemon(true);
        soundThread.start();
    }

    /**
     * pre:  none
     * post: the item-box battle sound stops immediately
     */
    public static void stop() {
        playing = false;
        if (soundProcess != null) {
            soundProcess.destroy();
            soundProcess = null;
        }
    }
}
