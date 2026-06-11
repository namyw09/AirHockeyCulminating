import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;

// launches the Python YOLO "candy battle" minigame and reads back the winner
public class CandyBattle {

    // absolute paths to the python environment, script, model, and result file
    private static final String PYTHON = "/opt/anaconda3/envs/yolo-env1/bin/python";
    private static final String SCRIPT = "/Users/brighton/Documents/yolo/my_model/candy_battle.py";
    private static final String MODEL  = "/Users/brighton/Documents/yolo/my_model/my_model.pt";
    private static final String RESULT = "/Users/brighton/Documents/yolo/my_model/candy_result.txt";
    private static String lastError = "";

    /**
     * runs the candy battle and returns which player won
     * pre:  the python env, script, model, and a working USB camera are available
     * post: returns 1 if Player 1 won, 2 if Player 2 won, or 0 if there is no
     *       winner or anything went wrong (so the match never gets stuck)
     */
    public static int run() {
        lastError = "";

        try {
            // remove any leftover result from a previous battle
            File resultFile = new File(RESULT);
            if (resultFile.exists()) {
                resultFile.delete();
            }

            String[] cameraSources = { "usb0", "usb1" };
            for (int i = 0; i < cameraSources.length; i++) {
                String output = runPythonBattle(cameraSources[i]);

                if (output.indexOf("ERROR: could not open camera") == -1) {
                    return readWinner(resultFile);
                }

                lastError = "Could not open camera source " + cameraSources[i] + ".";
            }

            lastError = "Could not open camera. Check macOS Camera permission for the app that launched the game.";
            return 0;
        } catch (Exception e) {
            // any failure (missing env, no camera, etc.) means no winner
            lastError = e.getMessage();
            return 0;
        }
    }

    /**
     * pre:  source is a camera source understood by candy_battle.py
     * post: python battle has run once; combined output is printed and returned
     */
    private static String runPythonBattle(String source) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                PYTHON, SCRIPT,
                "--model", MODEL,
                "--source", source,
                "--resolution", "1280x720",
                "--result", RESULT);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            output.append(line).append("\n");
        }
        reader.close();
        process.waitFor();

        return output.toString();
    }

    /**
     * post: returns the last camera/script error, or an empty string if there was none
     */
    public static String getLastError() {
        return lastError;
    }

    /**
     * pre:  resultFile may or may not exist
     * post: returns the integer after "winner=" in the file, or 0 if it is
     *       missing or cannot be parsed
     */
    private static int readWinner(File resultFile) {
        if (!resultFile.exists()) {
            return 0;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(resultFile));
            String line;
            int winner = 0;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("winner=")) {
                    winner = Integer.parseInt(line.substring("winner=".length()).trim());
                }
            }
            reader.close();
            return winner;
        } catch (Exception e) {
            return 0;
        }
    }
}
