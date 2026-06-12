import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.net.URISyntaxException;

// loads the Press Start 2P pixel font we bundled in, which is what gives the game its
// retro arcade look. caches it so we only load it off disk once
public class RetroFont {

    // the loaded base font, sized later with deriveFont; null if unavailable
    private static Font    baseFont      = null;
    private static boolean loadAttempted = false;

    /**
     * returns the retro pixel font at the requested size
     * pre:  size is positive
     * post: returns Press Start 2P at the given size if the bundled .ttf loaded,
     *       otherwise a bold Monospaced font of the same size as a fallback
     */
    public static Font get(float size) {
        loadBaseFont();
        if (baseFont != null) {
            return baseFont.deriveFont(size);
        }
        return new Font("Monospaced", Font.BOLD, (int) size);
    }

    /**
     * loads the bundled font file once and caches it
     * pre:  none
     * post: baseFont is set from assets/fonts/PressStart2P-Regular.ttf if it
     *       exists and is valid; otherwise baseFont stays null and the
     *       Monospaced fallback is used from then on
     */
    private static void loadBaseFont() {
        if (loadAttempted) {
            return;
        }
        loadAttempted = true;

        File fontFile = findFontFile();
        if (fontFile == null || !fontFile.exists()) {
            return;
        }

        try {
            baseFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            // gotta register it or Java won't actually let us use it everywhere
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(baseFont);
        } catch (Exception e) {
            // if the font is missing or broken it's fine, we just use plain Monospaced instead
            baseFont = null;
        }
    }

    /**
     * finds the bundled pixel font file inside the project assets
     * pre:  none
     * post: returns assets/fonts/PressStart2P-Regular.ttf next to the project,
     *       or null if the location cannot be resolved
     */
    private static File findFontFile() {
        try {
            File binDir = new File(
                RetroFont.class.getProtectionDomain()
                               .getCodeSource()
                               .getLocation()
                               .toURI());
            return new File(binDir.getParentFile(), "assets/fonts/PressStart2P-Regular.ttf");
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
