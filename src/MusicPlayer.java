import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

// plays background music using Java sound, so it works on Windows and Mac
// before i used afplay and i realized it only worked on mac, so now we used clip so it works on both instances
public class MusicPlayer {

    private static Clip currentClip = null;
    private static float volume = 1.0f;

    // starts looping one music file
    public static void startLoop(File musicFile) {
        stop();

        try {
            AudioInputStream audio = AudioSystem.getAudioInputStream(musicFile);
            currentClip = AudioSystem.getClip();
            currentClip.open(audio);
            setClipVolume(currentClip, volume);
            currentClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            currentClip = null;
        }
    }

    // a method that makes the music quieter
    public static void lowerVolume() {
        volume = 0.20f;
        if (currentClip != null) {
            setClipVolume(currentClip, volume);
        }
    }

    // stops the current music
    public static void stop() {
        if (currentClip != null) {
            currentClip.stop();
            currentClip.close();
            currentClip = null;
        }
        volume = 1.0f;
    }

    // main menu music
    // we have a wav for the main menu (startpanel) and one music for the actual game
    public static File findThemeFile() {
        return new File("assets/audio/theme.wav");
    }

    // main match music
    public static File findBattleMusicFile() {
        File battleMusic = new File("assets/audio/coconut-mall-battle-music.wav");
        if (battleMusic.exists()) {
            return battleMusic;
        }
        return null;
    }

    private static void setClipVolume(Clip clip, float amount) {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }

        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float safeAmount = Math.max(0.01f, Math.min(1.0f, amount));
        float decibels = (float) (20.0 * Math.log10(safeAmount));
        gain.setValue(decibels);
    }
}
