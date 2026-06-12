import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

// plays short wav sound effects from assets/audio
public class SoundEffects {

    public static void play(String name) {
        play(name, 1.0f);
    }

    public static void play(String name, float volume) {
        File soundFile = new File("assets/audio/" + name + ".wav");
        if (!soundFile.exists()) {
            return;
        }

        Thread soundThread = new Thread(() -> {
            try {
                AudioInputStream audio = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audio);
                clip.start();
            } catch (Exception e) {
                // sound effects are optional, so ignore audio errors
            }
        });
        soundThread.setDaemon(true);
        soundThread.start();
    }
}
