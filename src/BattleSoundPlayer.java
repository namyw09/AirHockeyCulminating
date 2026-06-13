import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

// plays the item-box sound during the candy battle
public class BattleSoundPlayer {

    private static final String SOUND_FILE = "assets/audio/item-box-camera-sound.wav";

    private static Clip currentClip = null;

    // starts looping the candy battle sound
    public static void startLoop() {
        stop();

        File soundFile = new File(SOUND_FILE);
        if (!soundFile.exists()) {
            return;
        }

        try {
            AudioInputStream audio = AudioSystem.getAudioInputStream(soundFile);
            currentClip = AudioSystem.getClip();
            currentClip.open(audio);
            currentClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            currentClip = null;
        }
    }

    // stops the candy battle sound
    public static void stop() {
        if (currentClip != null) {
            currentClip.stop();
            currentClip.close();
            currentClip = null;
        }
    }
}
