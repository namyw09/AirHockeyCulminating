import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;

// launches the Python YOLO candy battle and reads back the winner
// YOLO setup followed Evan Juras' Train and Deploy YOLO Models tutorial.
public class CandyBattle {

    // On Windows, "py" runs the installed Python.
    private static final String PYTHON = "py";

    // The game should be run from the project folder, so this relative path works.
    private static final File MODEL_DIR = new File("yolo/my_model");

    /**
     * runs the python candy battle and reads the winner
     * pre:  the python env, script, model, and a working USB camera are available;
     *       p1Name and p2Name are the players' display names
     * post: returns 1 if Player 1 won, 2 if Player 2 won, or 0 if there is no
     *       winner or anything went wrong (so the match never gets stuck)
     */
    public static int run(String p1Name, String p2Name) {
        File scriptFile = new File(MODEL_DIR, "candy_battle.py");
        File modelFile = new File(MODEL_DIR, "my_model.pt");
        File resultFile = new File(MODEL_DIR, "candy_result.txt");

        // Delete old results so we never read last round's winner by mistake.
        resultFile.delete();

        try {
            runPythonBattle(scriptFile, modelFile, resultFile, p1Name, p2Name);
            return readWinner(resultFile);
        } catch (Exception e) {
            // If Python or the camera fails, nobody wins and the Java game keeps going.
            return 0;
        }
    }

    /**
     * launches the python battle using the first USB camera
     * pre:  p1Name and p2Name are the players' display names
     * post: python battle has run once, and its output is printed to the terminal
     */
    private static void runPythonBattle(File scriptFile, File modelFile, File resultFile,
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

        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        reader.close();
        process.waitFor();
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
