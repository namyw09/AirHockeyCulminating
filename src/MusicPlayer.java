import java.io.File;
import java.util.concurrent.TimeUnit;

// plays background music with the Mac afplay command
public class MusicPlayer {

    private static Process currentSound = null;
    private static boolean playing = false;
    private static String volume = "1.00";

    // starts looping one music file
    public static void startLoop(File musicFile) {
        stop();

        if (musicFile == null || !musicFile.exists()) {
            return;
        }

        final String path = musicFile.getPath();
        volume = "1.00";
        playing = true;

        Thread musicThread = new Thread(() -> {
            while (playing) {
                try {
                    currentSound = new ProcessBuilder("afplay", "-v", volume, path).start();
                    currentSound.waitFor();
                } catch (Exception e) {
                    playing = false;
                }
            }
        });
        musicThread.setDaemon(true);
        musicThread.start();
    }

    // makes the next loop quieter
    public static void lowerVolume() {
        volume = "0.20";
    }

    // stops the current music
    public static void stop() {
        playing = false;
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

    // main menu music
    public static File findThemeFile() {
        return new File("assets/audio/theme.mp3");
    }

    // gets a file from assets/audio
    public static File findAudio(String fileName) {
        return new File("assets/audio/" + fileName);
    }

    // main match music
    public static File findBattleMusicFile() {
        File battleMusic = new File("assets/audio/coconut-mall-battle-music.mp3");
        if (battleMusic.exists()) {
            return battleMusic;
        }
        return null;
    }
}
