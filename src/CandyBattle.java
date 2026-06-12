import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;

// launches the Python YOLO "candy battle" minigame and reads back the winner
//
// The YOLO model run by the Python script was trained by us following Evan Juras'
// (EdjeElectronics) "Train and Deploy YOLO Models" tutorial. Full source list:
//   - YOLO training video:   https://www.youtube.com/watch?v=r0RspiLG260
//   - Tutorial repo:         https://github.com/EdjeElectronics/Train-and-Deploy-YOLO-Models
//   - Companion guide:       https://www.ejtech.io/learn/train-yolo-models
//   - Colab notebook:        https://colab.research.google.com/github/EdjeElectronics/Train-and-Deploy-YOLO-Models/blob/main/Train_YOLO_Models.ipynb
//   - Candy dataset:         https://s3.us-west-1.amazonaws.com/evanjuras.com/resources/candy_data_06JAN25.zip
//   - Label Studio:          https://labelstud.io/
//   - Ultralytics YOLO:      https://github.com/ultralytics/ultralytics  (docs: https://docs.ultralytics.com/)
public class CandyBattle {

    // path to the specific python env that has all the YOLO/camera stuff installed.
    // hardcoded because setting up the environment was honestly the hardest part of this
    private static final String PYTHON = "/opt/anaconda3/envs/yolo-env1/bin/python";

    // the project is always run from its top folder, so this relative path
    // points straight at the YOLO model files
    private static final File MODEL_DIR = new File("yolo/my_model");

    private static String lastError = "";

    /**
     * runs the python candy battle and reads the winner
     * pre:  the python env, script, model, and a working USB camera are available;
     *       p1Name and p2Name are the players' display names
     * post: returns 1 if Player 1 won, 2 if Player 2 won, or 0 if there is no
     *       winner or anything went wrong (so the match never gets stuck)
     */
    public static int run(String p1Name, String p2Name) {
        lastError = "";

        File scriptFile = new File(MODEL_DIR, "candy_battle.py");
        File modelFile = new File(MODEL_DIR, "my_model.pt");
        File resultFile = new File(MODEL_DIR, "candy_result.txt");

        // wipe out the result file from last time so we don't accidentally read an old winner
        resultFile.delete();

        try {
            String output = runPythonBattle(scriptFile, modelFile, resultFile, p1Name, p2Name);
            if (output.indexOf("ERROR: could not open camera") != -1) {
                lastError = "Could not open camera. Check macOS Camera permission for the app that launched the game.";
                return 0;
            }
            return readWinner(resultFile);
        } catch (Exception e) {
            // if anything goes wrong (no python, no camera, whatever) just say nobody won
            // so the match doesn't get stuck waiting forever
            lastError = e.getMessage();
            return 0;
        }
    }

    /**
     * launches the python battle using the first USB camera
     * pre:  p1Name and p2Name are the players' display names
     * post: python battle has run once; combined output is printed and returned
     */
    private static String runPythonBattle(File scriptFile, File modelFile, File resultFile,
            String p1Name, String p2Name) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                PYTHON, scriptFile.getPath(),
                "--model", modelFile.getPath(),
                "--source", "usb0",
                "--resolution", "1280x720",
                "--result", resultFile.getPath(),
                "--p1name", p1Name,
                "--p2name", p2Name);
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
