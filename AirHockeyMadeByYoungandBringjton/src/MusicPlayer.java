import java.io.File;
import java.net.URISyntaxException;

// plays background music via macOS afplay; supports fade-out
public class MusicPlayer {

    private static Process  currentProcess = null;
    private static Thread   loopThread     = null;
    private static String   musicFilePath  = null;
    private static long     startTimeMillis = 0;

    // afplay runs as a separate OS process, so it keeps playing after the JVM
    // exits — this shutdown hook kills it no matter how the game is quit
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> stop()));
    }

    /**
     * starts one music file once
     * pre:  none
     * post: music starts playing from the given file; any previous playback is stopped
     */
    public static void start(File musicFile) {
        start(musicFile, false);
    }

    /**
     * starts one music file on repeat
     * pre:  none
     * post: music loops from the given file until stop() or another track starts
     */
    public static void startLoop(File musicFile) {
        start(musicFile, true);
    }

    /**
     * starts music with optional looping
     * pre:  none
     * post: music starts from the given file; any previous playback is stopped
     */
    private static void start(File musicFile, boolean loop) {
        stop();
        if (musicFile == null || !musicFile.exists()) {
            return;
        }

        final String path = musicFile.getAbsolutePath();
        musicFilePath   = path;
        startTimeMillis = System.currentTimeMillis();

        loopThread = new Thread(() -> {
            try {
                do {
                    ProcessBuilder pb = new ProcessBuilder("afplay", path);
                    currentProcess = pb.start();
                    currentProcess.waitFor();
                } while (loop && path.equals(musicFilePath));
            } catch (Exception e) {
                // ignore
            }
        });
        loopThread.setDaemon(true);
        loopThread.start();
    }

    /**
     * lowers the current music volume
     * pre:  none
     * post: a new quiet afplay process starts at volume 0.2; after 300ms the old
     *       loud process is killed so the transition has no audible gap
     */
    public static void lowerVolume() {
        if (currentProcess == null || musicFilePath == null) {
            return;
        }

        final Process oldProcess = currentProcess;

        try {
            // start quiet process from the beginning — afplay has no seek flag
            currentProcess = new ProcessBuilder("afplay", "-v", "0.20", musicFilePath).start();
            startTimeMillis = System.currentTimeMillis();
        } catch (Exception e) {
            return;
        }

        // let the new process produce audio for 300ms before killing the old loud one
        Thread overlap = new Thread(() -> {
            try { Thread.sleep(300); } catch (InterruptedException e) { /* ignore */ }
            oldProcess.destroy();
        });
        overlap.setDaemon(true);
        overlap.start();
    }

    /**
     * fades out the current music
     * pre:  none
     * post: music fades out over ~1 second by restarting afplay at the current
     *       position with decreasing volume; playback stops when volume reaches zero
     */
    public static void fadeOut() {
        if (currentProcess == null || musicFilePath == null) {
            return;
        }

        // capture position before we start the fade
        final float posSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000.0f;
        final String path = musicFilePath;

        // mark as done so nothing else restarts it
        musicFilePath = null;

        Thread fadeThread = new Thread(() -> {
            float[]  volumes  = { 0.55f, 0.30f, 0.12f, 0.04f };
            int      stepMs   = 220;
            float    pos      = posSeconds;

            for (float vol : volumes) {
                try {
                    if (currentProcess != null) {
                        currentProcess.destroy();
                    }
                    // -s seeks to the current position so the melody stays in sync
                    ProcessBuilder pb = new ProcessBuilder(
                            "afplay",
                            "-v", String.format("%.2f", vol),
                            "-s", String.format("%.2f", pos),
                            path);
                    currentProcess = pb.start();
                    Thread.sleep(stepMs);
                    pos += stepMs / 1000.0f;
                } catch (Exception e) {
                    break;
                }
            }

            if (currentProcess != null) {
                currentProcess.destroy();
                currentProcess = null;
            }
        });
        fadeThread.setDaemon(true);
        fadeThread.start();
    }

    /**
     * stops the current music
     * pre:  none
     * post: music stops immediately
     */
    public static void stop() {
        musicFilePath = null;
        if (currentProcess != null) {
            currentProcess.destroy();
            currentProcess = null;
        }
    }

    /**
     * finds the normal menu theme file
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

    /**
     * finds the battle music file
     * pre:  none
     * post: returns the requested battle-stage music file, or null if missing
     */
    public static File findBattleMusicFile() {
        File battleMusic = new File("/Users/brighton/Downloads/Coconut Mall (From Mario Kart Wii).mp3");
        if (battleMusic.exists()) {
            return battleMusic;
        }
        return null;
    }
}
