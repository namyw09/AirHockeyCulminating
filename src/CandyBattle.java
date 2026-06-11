import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;

// launches the Python YOLO "candy battle" minigame and reads back the winner
public class CandyBattle {

    // python environment used to run the YOLO candy battle script
    private static final String PYTHON = "/opt/anaconda3/envs/yolo-env1/bin/python";
    private static String lastError = "";

    /**
     * runs the python candy battle and reads the winner
     * pre:  the python env, script, model, and a working USB camera are available
     * post: returns 1 if Player 1 won, 2 if Player 2 won, or 0 if there is no
     *       winner or anything went wrong (so the match never gets stuck)
     */
    public static int run() {
        lastError = "";

        try {
            // remove any leftover result from a previous battle
            File modelDir = findYoloModelDir();
            File scriptFile = new File(modelDir, "candy_battle.py");
            File modelFile = new File(modelDir, "my_model.pt");
            File resultFile = new File(modelDir, "candy_result.txt");

            if (resultFile.exists()) {
                resultFile.delete();
            }

            String output = runPythonBattle("usb0", scriptFile, modelFile, resultFile);
            if (output.indexOf("ERROR: could not open camera") == -1) {
                return readWinner(resultFile);
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
     * launches the python battle for one camera source
     * pre:  source is a camera source understood by candy_battle.py
     * post: python battle has run once; combined output is printed and returned
     */
    private static String runPythonBattle(String source, File scriptFile,
            File modelFile, File resultFile) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                PYTHON, scriptFile.getAbsolutePath(),
                "--model", modelFile.getAbsolutePath(),
                "--source", source,
                "--resolution", "1280x720",
                "--result", resultFile.getAbsolutePath());
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
     * finds the moved YOLO model folder inside the project
     * pre:  the game is running from the compiled bin folder
     * post: returns the yolo/my_model folder inside the project folder
     */
    private static File findYoloModelDir() throws Exception {
        File binDir = new File(
                CandyBattle.class.getProtectionDomain()
                                  .getCodeSource()
                                  .getLocation()
                                  .toURI());
        File projectDir = binDir.getParentFile();
        return new File(projectDir, "yolo/my_model");
    }

    /**
     * gets the most recent camera or script error
     * pre:  none
     * post: returns the last camera/script error, or an empty string if there was none
     */
    public static String getLastError() {
        return lastError;
    }

    /**
     * reads the winner value from the result file
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
