import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

// plays short retro WAV sound effects with low latency and overlapping playback.
// WAV files live in assets/audio and are named <effect>.wav (e.g. puck-hit.wav).
// Uses javax.sound.sampled so effects can overlap and play on any platform.
public class SoundEffects {

    // raw bytes of each loaded effect, keyed by name (e.g. "puck-hit")
    private static final Map<String, byte[]>  CACHE   = new HashMap<String, byte[]>();
    // names whose file was missing, so we only check the disk once
    private static final Map<String, Boolean> MISSING = new HashMap<String, Boolean>();

    /**
     * plays a named effect at full volume
     * pre:  name matches a wav in assets/audio (without the extension)
     * post: the effect plays once if its file exists; otherwise nothing happens
     */
    public static void play(String name) {
        play(name, 1.0f);
    }

    /**
     * plays a named effect at the given volume
     * pre:  name matches a wav in assets/audio; volume is between 0 and 1
     * post: the effect plays once at the scaled volume if the file exists,
     *       overlapping any other effects; missing files are silently ignored
     */
    public static void play(String name, float volume) {
        final byte[] data = loadBytes(name);
        if (data == null) {
            return;
        }

        final float vol = Math.max(0f, Math.min(1f, volume));

        // open and start on a daemon thread so the game loop never blocks
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    AudioInputStream ais = AudioSystem.getAudioInputStream(
                            new ByteArrayInputStream(data));
                    final Clip clip = AudioSystem.getClip();
                    clip.open(ais);
                    applyVolume(clip, vol);
                    // close the line once playback finishes so clips don't leak
                    clip.addLineListener(new LineListener() {
                        public void update(LineEvent event) {
                            if (event.getType() == LineEvent.Type.STOP) {
                                clip.close();
                            }
                        }
                    });
                    clip.start();
                } catch (Exception e) {
                    // ignore audio failures so the game keeps running
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    /**
     * scales a clip's volume using its master gain control
     * pre:  clip is open
     * post: clip volume is set toward the given fraction (1 = full volume),
     *       or left unchanged if the gain control is not available
     */
    private static void applyVolume(Clip clip, float volume) {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }

        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

        // convert a 0..1 fraction into decibels (0 dB = full volume)
        float dB;
        if (volume <= 0.0001f) {
            dB = gain.getMinimum();
        } else {
            dB = (float) (Math.log10(volume) * 20.0);
        }

        if (dB < gain.getMinimum()) {
            dB = gain.getMinimum();
        }
        if (dB > gain.getMaximum()) {
            dB = gain.getMaximum();
        }
        gain.setValue(dB);
    }

    /**
     * loads and caches the bytes for one effect
     * pre:  none
     * post: returns the wav bytes for the effect (loading once), or null if the
     *       file is missing
     */
    private static byte[] loadBytes(String name) {
        if (CACHE.containsKey(name)) {
            return CACHE.get(name);
        }
        if (MISSING.containsKey(name)) {
            return null;
        }

        File file = findAudioFile(name + ".wav");
        if (file == null || !file.exists()) {
            MISSING.put(name, Boolean.TRUE);
            return null;
        }

        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            byte[] data = readAll(bis);
            bis.close();
            CACHE.put(name, data);
            return data;
        } catch (Exception e) {
            MISSING.put(name, Boolean.TRUE);
            return null;
        }
    }

    /**
     * reads every byte from a stream
     * pre:  in is open
     * post: returns all bytes read from the stream
     */
    private static byte[] readAll(BufferedInputStream in) throws java.io.IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    /**
     * finds an audio file inside the project assets
     * pre:  fileName includes the extension
     * post: returns the file under assets/audio, or null if unresolved
     */
    private static File findAudioFile(String fileName) {
        try {
            File binDir = new File(
                SoundEffects.class.getProtectionDomain()
                                  .getCodeSource()
                                  .getLocation()
                                  .toURI());
            return new File(binDir.getParentFile(), "assets/audio/" + fileName);
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
