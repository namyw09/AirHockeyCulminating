import java.io.File;

// plays a temporary sound layer during the candy battle only
public class BattleSoundPlayer {

    private static final String ITEM_BOX_SOUND =
            "/Users/brighton/Downloads/Mario Kart Wii - Item Box - Sound Effect - Lapraniteon (128k).mp3";

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

        File soundFile = new File(ITEM_BOX_SOUND);
        if (!soundFile.exists()) {
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
                // ignore audio failures so the game keeps running
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
}
