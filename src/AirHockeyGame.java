/*
 * Air Hockey - ICS3U Culminating Project
 * Authors: Brighton Ng + Youngwoo Nam
 *
 * Sources (resources used to learn skills for this project):
 *   The Candy Battle minigame uses a custom YOLO object-detection model we
 *   trained ourselves on photos of candy. The training process was learned from
 *   Evan Juras' (EdjeElectronics) "Train and Deploy YOLO Models" tutorial:
 *     - YOLO training video:    https://www.youtube.com/watch?v=r0RspiLG260
 *     - Tutorial repo:          https://github.com/EdjeElectronics/Train-and-Deploy-YOLO-Models
 *     - Companion guide:        https://www.ejtech.io/learn/train-yolo-models
 *     - Colab training notebook:https://colab.research.google.com/github/EdjeElectronics/Train-and-Deploy-YOLO-Models/blob/main/Train_YOLO_Models.ipynb
 *     - Candy dataset:          https://s3.us-west-1.amazonaws.com/evanjuras.com/resources/candy_data_06JAN25.zip
 *     - Label Studio (image labelling): https://labelstud.io/
 *     - Ultralytics YOLO library:       https://github.com/ultralytics/ultralytics
 *     - Ultralytics docs:               https://docs.ultralytics.com/
 *     - OpenCV (camera capture):        https://opencv.org/
 *     - PyTorch (YOLO backend):         https://pytorch.org/get-started/locally/
 *     - Anaconda (Python environment):  https://www.anaconda.com/download
 *
 *   Retro theme assets:
 *     - "Press Start 2P" pixel font (SIL OFL): https://fonts.google.com/specimen/Press+Start+2P
 *     - Sound effects (retro blips/buzzer):    generated/sourced via https://sfxr.me/ , https://kenney.nl/ (CC0)
 *     - Music loops (match / final / sudden death): royalty-free, see assets/audio
 *
 *   The final-seconds 5-4-3-2-1 build-up and the sudden-death overtime music
 *   change were inspired by the mobile game Clash Royale.
 */
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import framework.Game;

// brighton ng + youngwoo nam - ics3u culminating
// main game class, all the actual game logic is here
public class AirHockeyGame extends Game {

    // rink dimensions - we figured these out by trial and error until it looked right
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final int RINK_X = 50;
    private static final int RINK_Y = 80;
    private static final int RINK_WIDTH = 700;
    private static final int RINK_HEIGHT = 440;
    private static final int GOAL_HEIGHT = 150;
    private static final int MATCH_LENGTH_SECONDS = 60;
    private static final int SCORE_LIMIT = 7;

    private Rink rink;
    private CursorControlledPaddle playerPaddle;
    private CursorControlledPaddle opponentPaddle;
    private ArrayList<Puck> pucks = new ArrayList<Puck>();

    // current score for each player
    private int player1Score = 0;
    private int player2Score = 0;
    private boolean gameOver = false;

    // final-seconds intensity and sudden-death overtime (Clash Royale style)
    private static final int FINAL_COUNTDOWN_SECONDS = 5;     // when the 5-4-3-2-1 begins
    private boolean suddenDeath        = false;  // true once a tie forces overtime
    private boolean finalIntensityOn   = false;  // true once the final-seconds music/pulse starts
    private boolean matchMusicStarted  = false;  // true once the main match loop has begun
    private int     lastCountdownSecond = -1;    // last whole second announced in the final countdown

    // powerup state
    private Powerup currentPowerup       = null;
    private long    lastPowerupEndTime   = 0;
    private boolean player1Grown         = false;
    private long    player1GrowStart     = 0;
    private boolean player2Grown         = false;
    private long    player2GrowStart     = 0;
    private boolean playerPaddleSpeedy   = false;
    private long    playerSpeedyStart    = 0;
    private boolean opponentPaddleSpeedy = false;
    private long    opponentSpeedyStart  = 0;
    private boolean playerPaddleSlowed   = false;
    private long    playerSlowedStart    = 0;
    private boolean opponentPaddleSlowed = false;
    private long    opponentSlowedStart  = 0;
    private Random  random               = new Random();

    // opening and multi-puck pacing
    private static final int COUNTDOWN_MS = 3200;
    private long    countdownStartTime = 0;
    private boolean countdownDone      = false;
    private boolean secondPuckAdded    = false;
    private boolean thirdPuckAdded     = false;

    // default names in case the player skips the input
    private String player1Name = "Player 1";
    private String player2Name = "Player 2";

    // candy battle minigame state
    private int     candyBattleTriggerRemaining = 0;     // fires when this many seconds remain
    private boolean candyBattleDone             = false; // only happens once per match
    private int     pendingSpecialPowerupPlayer = 0;     // hook for the special powerup (coded later)
    private int     cursorControlPlayer         = 0;     // 0 = none, 1/2 = candy winner uses mouse
    private int     cursorX                      = RINK_X + RINK_WIDTH / 2;
    private int     cursorY                      = RINK_Y + RINK_HEIGHT / 2;

    /**
     * sets up the window, timer, player names, and starting objects
     * pre:  the game frame exists but nothing has been added yet
     * post: the rink, starting puck, paddles, and 0-0 scoreboard are ready
     */
    public void setup() {
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setTitle("Air Hockey");
        setBackground(new Color(20, 30, 48));
        setDelay(16);
        setMatchLengthSeconds(MATCH_LENGTH_SECONDS);
        setupMouseTracking();

        // show the controls before anything starts
        JOptionPane.showMessageDialog(this,
                "Player 1 (Blue): W / A / S / D\nPlayer 2 (Red): Arrow Keys\n\n"
                + "Secret Candy Battle reward: the winner controls their paddle with the mouse for the rest of the match.\n\n"
                + "Score by hitting the puck into the other side's goal.",
                "How to Play", JOptionPane.INFORMATION_MESSAGE);

        player1Name = promptForName("Enter Player 1's name:", "Player 1");
        player2Name = promptForName("Enter Player 2's name:", "Player 2");

        // puck and paddles are added before the rink so they stay visible
        addPuck(1, false);

        playerPaddle = new CursorControlledPaddle(
                RINK_X + 80,
                RINK_Y + RINK_HEIGHT / 2,
                new Color(54, 124, 230));

        opponentPaddle = new CursorControlledPaddle(
                RINK_X + RINK_WIDTH - 80,
                RINK_Y + RINK_HEIGHT / 2,
                new Color(220, 70, 60));

        rink = new Rink(WINDOW_WIDTH, WINDOW_HEIGHT,
                RINK_X, RINK_Y, RINK_WIDTH, RINK_HEIGHT, GOAL_HEIGHT,
                player1Name, player2Name);

        updateScoreboard();

        add(playerPaddle);
        add(opponentPaddle);
        add(rink);
        rink.setCenterMessage("3");

        lastPowerupEndTime = System.currentTimeMillis();

        // candy battle fires once, when 15-30 seconds are left on the clock
        candyBattleTriggerRemaining = 15 + random.nextInt(16);

        PauseButton pauseBtn = new PauseButton(WINDOW_WIDTH - 110, 8, () -> showPauseDialog());
        add(pauseBtn);
        getContentPane().setComponentZOrder(pauseBtn, 0);
    }

    /**
     * tracks the mouse for cursor-control powerups
     * pre:  content pane exists
     * post: latest mouse position is tracked in game-coordinate space
     */
    private void setupMouseTracking() {
        MouseAdapter mouseTracker = new MouseAdapter() {
            public void mouseMoved(MouseEvent e) {
                cursorX = e.getX();
                cursorY = e.getY();
            }

            public void mouseDragged(MouseEvent e) {
                cursorX = e.getX();
                cursorY = e.getY();
            }
        };

        getContentPane().addMouseMotionListener(mouseTracker);
    }

    /**
     * runs one frame of the game
     * pre:  setup() is done and all game objects exist
     * post: movement, goals, and collisions are checked, or the game ends if time/score is up
     */
    public void act() {
        updateScoreboard();

        if (gameOver) {
            return;
        }

        // regulation time ran out: either finish, or start sudden-death overtime
        // on a tie. once in sudden death the clock is ignored and play continues.
        if (!suddenDeath && isTimeUp()) {
            handleTimeUp();
            return;
        }

        if (!countdownDone) {
            handleStartCountdown();
            return;
        }

        // surprise candy battle once the clock drops into the trigger window
        if (!suddenDeath && !candyBattleDone
                && getTimeRemainingSeconds() <= candyBattleTriggerRemaining) {
            candyBattleDone = true;
            startCandyBattle();
            return; // game is paused now; skip the rest of this frame
        }

        // ramp up the atmosphere in the final few seconds of regulation
        if (!suddenDeath) {
            handleFinalCountdown();
        }

        handleInput();
        addTimedExtraPucks();
        updatePucks();
        handleGoals();
        if (gameOver) {
            return;
        }
        handleWallCollisions();
        handlePaddleCollisions();
        handlePowerup();
    }

    /**
     * controls the one-time opening countdown
     * pre:  rink and first puck exist
     * post: countdown text updates; when it finishes, timer is reset and puck play begins
     */
    private void handleStartCountdown() {
        if (countdownStartTime == 0) {
            countdownStartTime = System.currentTimeMillis();
        }

        // hold the match clock at full time while 3-2-1-GO counts down so the
        // timer only starts ticking once play actually begins
        resetGameTimer();

        long elapsed = System.currentTimeMillis() - countdownStartTime;
        String message;

        if (elapsed < 1000) {
            message = "3";
        } else if (elapsed < 2000) {
            message = "2";
        } else if (elapsed < 3000) {
            message = "1";
        } else {
            message = "GO!";
        }

        rink.setCenterMessage(message);

        if (elapsed >= COUNTDOWN_MS) {
            countdownDone = true;
            rink.setCenterMessage("");
            resetGameTimer();
            serveAllPucks();
            startMatchMusic();
        }
    }

    /**
     * starts the main match music loop once play begins
     * pre:  the opening countdown has finished
     * post: the match-music track loops (falling back to the coconut-mall track);
     *       if neither file exists the current menu music simply keeps playing
     */
    private void startMatchMusic() {
        if (matchMusicStarted) {
            return;
        }
        matchMusicStarted = true;

        File match = MusicPlayer.findAudio("match-music.mp3");
        if (match == null || !match.exists()) {
            match = MusicPlayer.findBattleMusicFile(); // coconut-mall fallback
        }
        if (match != null && match.exists()) {
            MusicPlayer.startLoop(match);
        }
    }

    /**
     * runs the tense 5-4-3-2-1 ending of regulation
     * pre:  play is active and it is not yet sudden death
     * post: in the final seconds the music swaps to the intense loop, the rink
     *       pulses red, and a big number plus a tick sound fires on each new second
     */
    private void handleFinalCountdown() {
        int remaining = getTimeRemainingSeconds();
        if (remaining > FINAL_COUNTDOWN_SECONDS) {
            return;
        }

        // first frame of the final stretch: intense music + red pulse
        if (!finalIntensityOn) {
            finalIntensityOn = true;
            rink.setFinalCountdown(true);
            File intense = MusicPlayer.findAudio("final-intense.mp3");
            if (intense != null && intense.exists()) {
                MusicPlayer.startLoop(intense);
            }
        }

        // one big number and one tick each time the whole second changes
        if (remaining > 0 && remaining != lastCountdownSecond) {
            lastCountdownSecond = remaining;
            rink.setCenterMessage(String.valueOf(remaining));
            SoundEffects.play("countdown-tick");
        }
    }

    /**
     * decides what happens when regulation time runs out
     * pre:  the match clock has reached zero and it is not yet sudden death
     * post: a buzzer plays; a clear winner ends the game, while a tie starts
     *       sudden-death overtime
     */
    private void handleTimeUp() {
        SoundEffects.play("buzzer");
        rink.setFinalCountdown(false);
        rink.setCenterMessage("");

        if (player1Score == player2Score) {
            enterSuddenDeath();
        } else {
            finishGame("Time's Up");
        }
    }

    /**
     * begins sudden-death overtime after a tie
     * pre:  the score is tied and regulation time is up
     * post: overtime music plays, the rink shows the SUDDEN DEATH treatment, and
     *       the next goal will end the match
     */
    private void enterSuddenDeath() {
        suddenDeath = true;
        rink.setSuddenDeath(true);

        File music = MusicPlayer.findAudio("sudden-death.mp3");
        if (music != null && music.exists()) {
            MusicPlayer.startLoop(music);
        }

        File stinger = MusicPlayer.findAudio("sudden-death-stinger.wav");
        if (stinger != null && stinger.exists()) {
            SoundEffects.play("sudden-death-stinger");
        } else {
            SoundEffects.play("buzzer");
        }

        JOptionPane.showMessageDialog(this,
                "TIME'S UP - " + player1Score + " to " + player2Score + "\n\n"
                + ">>> SUDDEN DEATH <<<\nNext goal wins!",
                "Sudden Death", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * asks for a player name until one is entered
     * pre:  message is a valid prompt string
     * post: returns a non-empty name; loops until the player types one
     */
    private String promptForName(String message, String defaultName) {
        String name = "";

        while (name.trim().isEmpty()) {
            name = JOptionPane.showInputDialog(this,
                    message, "Player Names", JOptionPane.PLAIN_MESSAGE);

            if (name == null) {
                name = "";
            }

            if (name.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "A name is required to continue. Please enter a name.",
                        "Name Required", JOptionPane.WARNING_MESSAGE);
            }
        }

        return name.trim();
    }

    /**
     * sends the latest score and timer to the rink
     * pre:  the rink might not be created yet
     * post: if the rink exists, the scoreboard matches the game
     */
    private void updateScoreboard() {
        if (rink != null) {
            rink.setScoreboard(player1Score, player2Score, getFormattedTimeRemaining());
        }
    }

    /**
     * shows the pause menu and handles the selected action
     * pre:  game is running
     * post: game is paused and a dialog with Continue / Test Candy Battle / Quit
     *       is shown; Continue resumes, Test Candy Battle starts the camera
     *       minigame now, and Quit returns to the home screen without music
     */
    private void showPauseDialog() {
        pauseGame();

        Object[] options = { "Continue", "Test Candy Battle", "Quit to Menu" };
        int choice = JOptionPane.showOptionDialog(
                this,
                "The game is paused.",
                "Paused",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0 || choice == JOptionPane.CLOSED_OPTION) {
            resumeGame();
        } else if (choice == 1) {
            candyBattleDone = true;
            startCandyBattle();
        } else {
            MusicPlayer.stop();
            dispose();
            AirHockeyApp.showHomeQuiet();
        }
    }

    /**
     * runs the candy battle and applies its reward
     * pauses the match and runs the camera candy battle minigame
     * pre:  the game is running and the YOLO python script is available
     * post: the game is paused, the python minigame runs on a background thread,
     *       and once it finishes the winner is announced, the special powerup is
     *       granted, and the match resumes
     */
    private void startCandyBattle() {
        pauseGame();

        // keep the menu music playing continuously underneath - don't restart it

        JOptionPane.showMessageDialog(this,
                "CANDY BATTLE!\n\nEach player: hold up your 2 candies to the camera.\n"
                + "Player 1 on the LEFT, Player 2 on the RIGHT.\n"
                + "The computer secretly picked one - whoever is holding it wins a special powerup!\n\n"
                + "Get ready... the camera opens when you click OK.",
                "Candy Battle", JOptionPane.INFORMATION_MESSAGE);

        // run the camera/YOLO process off the EDT so the UI thread is not blocked
        Thread battleThread = new Thread(() -> {
            // after the player clicks OK on the dialog above, hold for a short
            // silent delay while the camera + model load, then play the item-box
            // sound right as the camera opens
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            BattleSoundPlayer.startLoop();
            int winner = CandyBattle.run();
            BattleSoundPlayer.stop();

            SwingUtilities.invokeLater(() -> {
                if (winner == 1 || winner == 2) {
                    String name = (winner == 1) ? player1Name : player2Name;
                    JOptionPane.showMessageDialog(this,
                            name + " held the right candy and wins a special powerup!",
                            "Candy Battle Result", JOptionPane.INFORMATION_MESSAGE);
                    pendingSpecialPowerupPlayer = winner;
                    applySpecialPowerup(winner);
                } else {
                    String error = CandyBattle.getLastError();
                    String message = "No winner this round - no special powerup awarded.";
                    if (error != null && error.trim().isEmpty() == false) {
                        message = message + "\n\nCamera note: " + error;
                    }
                    JOptionPane.showMessageDialog(this,
                            message,
                            "Candy Battle Result", JOptionPane.INFORMATION_MESSAGE);
                }

                resumeGame();
            });
        });
        battleThread.setDaemon(true);
        battleThread.start();
    }

    /**
     * gives the candy battle winner cursor control
     * grants the candy battle's special powerup to the winning player
     * pre:  player is 1 or 2
     * post: winner controls their paddle with the mouse for the rest of the match
     */
    private void applySpecialPowerup(int player) {
        pendingSpecialPowerupPlayer = player;
        cursorControlPlayer = player;

        String name = (player == 1) ? player1Name : player2Name;
        JOptionPane.showMessageDialog(this,
                name + " unlocked cursor control for the rest of the match!",
                "Secret Powerup", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * stops the match and shows who won
     * pre:  time is up or someone reached the score limit
     * post: the game loop stops and the final result pops up
     */
    private void finishGame(String reason) {
        gameOver = true;
        stopGame();
        updateScoreboard();

            // if condiiton to determine the winner, also checks for tie game possibility

        String result;
        if (player1Score > player2Score) {
            result = player1Name + " wins!";
        } else if (player2Score > player1Score) {
            result = player2Name + " wins!";
        } else {
            result = "Tie game!";
        }

        //display the finak score of the game
        JOptionPane.showMessageDialog(this,
                result + "\nFinal Score: " + player1Name + " " + player1Score
                        + " - " + player2Score + " " + player2Name,
                reason, JOptionPane.INFORMATION_MESSAGE);

        saveMatchResult(result);
        dispose();
        AirHockeyApp.showHome();
    }

    /**
     * saves the match result to history
     * pre:  player names and scores are set; result is the outcome string
     * post: one line is appended to match_history.txt with the date, names,
     *       score, and winner; silently does nothing if the file cannot be written
     */
    private void saveMatchResult(String result) {
        try {
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
            String line = "[" + date + "]  "
                    + player1Name + " " + player1Score
                    + "  -  "
                    + player2Score + " " + player2Name
                    + "   ->   " + result;
            FileWriter fw = new FileWriter("match_history.txt", true);
            fw.write(line + "\n");
            fw.close();
        } catch (IOException e) {
            // match history is non-critical; ignore write failures
        }
    }

    /**
     * moves both paddles using the keys being held
     * pre:  both paddles exist and Game has the current key states
     * post: each paddle stays inside its side of the rink
     */
    private void handleInput() {
        if (cursorControlPlayer == 1) {
            playerPaddle.followCursor(
                    cursorX, cursorY,
                    RINK_X, RINK_X + RINK_WIDTH / 2,
                    RINK_Y, RINK_Y + RINK_HEIGHT);
        } else {
            playerPaddle.move(
                    WKeyPressed(), SKeyPressed(), AKeyPressed(), DKeyPressed(),
                    RINK_X, RINK_X + RINK_WIDTH / 2,
                    RINK_Y, RINK_Y + RINK_HEIGHT);
        }

        if (cursorControlPlayer == 2) {
            opponentPaddle.followCursor(
                    cursorX, cursorY,
                    RINK_X + RINK_WIDTH / 2, RINK_X + RINK_WIDTH,
                    RINK_Y, RINK_Y + RINK_HEIGHT);
        } else {
            opponentPaddle.move(
                    UpKeyPressed(), DownKeyPressed(), LeftKeyPressed(), RightKeyPressed(),
                    RINK_X + RINK_WIDTH / 2, RINK_X + RINK_WIDTH,
                    RINK_Y, RINK_Y + RINK_HEIGHT);
        }
    }

    /**
     * adds a puck at center and optionally serves it right away
     * pre:  rink dimensions are initialized
     * post: a new puck is added above the rink layer and included in puck updates
     */
    private void addPuck(int direction, boolean serveNow) {
        Puck newPuck = new Puck(
                RINK_X + RINK_WIDTH / 2,
                RINK_Y + RINK_HEIGHT / 2);

        if (serveNow) {
            newPuck.serve(direction, random);
        }

        pucks.add(newPuck);
        add(newPuck);
        if (rink != null) {
            getContentPane().setComponentZOrder(newPuck, getContentPane().getComponentCount() - 2);
        }
    }

    /**
     * serves every puck that is already on the rink
     * pre:  at least one puck exists
     * post: each puck gets a randomized diagonal serve
     */
    private void serveAllPucks() {
        for (int i = 0; i < pucks.size(); i++) {
            int direction = (i % 2 == 0) ? 1 : -1;
            pucks.get(i).serve(direction, random);
        }
    }

    /**
     * adds extra pucks as the match clock reaches ramp moments
     * pre:  countdown is done and timer is running
     * post: second puck appears at 40 seconds, third puck appears at 20 seconds
     */
    private void addTimedExtraPucks() {
        int remaining = getTimeRemainingSeconds();

        if (!secondPuckAdded && remaining <= 40) {
            secondPuckAdded = true;
            addPuck(-1, true);
        }

        if (!thirdPuckAdded && remaining <= 20) {
            thirdPuckAdded = true;
            addPuck(1, true);
        }
    }

    /**
     * updates all active pucks
     * pre:  pucks list exists
     * post: every puck moves and applies its own speed physics
     */
    private void updatePucks() {
        for (int i = 0; i < pucks.size(); i++) {
            pucks.get(i).update();
        }
    }

    /**
     * bounces all pucks off the rink walls, except where the goals are
     * pre:  pucks already moved this frame
     * post: each puck is pushed back in bounds and bounces if it hit a wall
     */
    private void handleWallCollisions() {
        for (int i = 0; i < pucks.size(); i++) {
            handleWallCollision(pucks.get(i));
        }
    }

    /**
     * bounces one puck off the rink walls, except where the goals are
     * pre:  puck already moved this frame
     * post: puck is pushed back in bounds and bounces if it hit a wall
     */
    private void handleWallCollision(Puck puck) {
        int goalTop = RINK_Y + (RINK_HEIGHT - GOAL_HEIGHT) / 2;
        int goalBottom = goalTop + GOAL_HEIGHT;
        int puckDiameter = puck.getRadius() * 2;

        boolean inGoalOpening = false;
        if (puck.getCenterY() >= goalTop && puck.getCenterY() <= goalBottom) {
            inGoalOpening = true;
        }

        boolean bounced = false;

        // top wall
        if (puck.getY() <= RINK_Y) {
            puck.setPuckY(RINK_Y);
            puck.bounceVertical();
            bounced = true;
        }

        // bottom wall
        if (puck.getY() + puckDiameter >= RINK_Y + RINK_HEIGHT) {
            puck.setPuckY(RINK_Y + RINK_HEIGHT - puckDiameter);
            puck.bounceVertical();
            bounced = true;
        }

        // left and right walls - skip if the puck is lined up with the goal
        if (puck.getX() <= RINK_X && inGoalOpening == false) {
            puck.setPuckX(RINK_X);
            puck.bounceHorizontal();
            bounced = true;
        }

        if (puck.getX() + puckDiameter >= RINK_X + RINK_WIDTH && inGoalOpening == false) {
            puck.setPuckX(RINK_X + RINK_WIDTH - puckDiameter);
            puck.bounceHorizontal();
            bounced = true;
        }

        if (bounced) {
            SoundEffects.play("wall-bounce", 0.5f);
        }
    }

    /**
     * updates the score if any puck goes into a goal
     * pre:  pucks exist and might be lined up with a goal
     * post: scores update and only the scoring puck resets
     */
    private void handleGoals() {
        for (int i = 0; i < pucks.size(); i++) {
            handleGoal(pucks.get(i));
            if (gameOver) {
                return;
            }
        }
    }

    /**
     * updates the score if one puck goes into a goal
     * pre:  puck exists and might be lined up with a goal
     * post: the right score goes up, scoreboard updates, and puck resets
     */
    private void handleGoal(Puck puck) {
        int goalTop      = RINK_Y + (RINK_HEIGHT - GOAL_HEIGHT) / 2;
        int goalBottom   = goalTop + GOAL_HEIGHT;
        int puckDiameter = puck.getRadius() * 2;

        boolean inGoalOpening = false;
        if (puck.getCenterY() >= goalTop && puck.getCenterY() <= goalBottom) {
            inGoalOpening = true;
        }

        if (inGoalOpening == false) {
            return;
        }

        // puck crossed the left goal line
        if (puck.getX() + puckDiameter < RINK_X) {
            player2Score++;
            onGoalScored();
            if (gameOver) {
                return;
            }
            puck.reset(RINK_X + RINK_WIDTH / 2, RINK_Y + RINK_HEIGHT / 2, -1, random);
        }

        // puck crossed the right goal line
        if (puck.getX() > RINK_X + RINK_WIDTH) {
            player1Score++;
            onGoalScored();
            if (gameOver) {
                return;
            }
            puck.reset(RINK_X + RINK_WIDTH / 2, RINK_Y + RINK_HEIGHT / 2, 1, random);
        }
    }

    /**
     * shared reaction to any goal being scored
     * pre:  a score was just incremented
     * post: the goal sound plays and the scoreboard updates; the match ends if
     *       this goal wins sudden death or reaches the score limit
     */
    private void onGoalScored() {
        SoundEffects.play("goal");
        updateScoreboard();

        if (suddenDeath) {
            finishGame("Sudden Death");
            return;
        }

        checkScoreLimit();
    }

    /**
     * checks if a player reached the score limit
     * pre:  a goal was just scored
     * post: the game ends if either player has 7 points
     */
    private void checkScoreLimit() {
        if (player1Score >= SCORE_LIMIT || player2Score >= SCORE_LIMIT) {
            finishGame("Scoref Limit Reached");
        }
    }

    /**
     * picks a random position in one half of the rink and spawns a new powerup there
     * pre:  rink is initialized; random is ready
     * post: currentPowerup is set to a new active powerup and added to the game above the rink layer
     */
    private void spawnPowerup() {
        int rinkCenterX = RINK_X + RINK_WIDTH / 2;

        int half = random.nextInt(2);
        int owner;
        int spawnMinX;
        int spawnMaxX;

        if (half == 0) {
            owner     = 1;
            spawnMinX = RINK_X + 80;
            spawnMaxX = rinkCenterX - Powerup.RADIUS;
        } else {
            owner     = 2;
            spawnMinX = rinkCenterX + Powerup.RADIUS;
            spawnMaxX = RINK_X + RINK_WIDTH - 80;
        }

        int spawnMinY = RINK_Y + 20;
        int spawnMaxY = RINK_Y + RINK_HEIGHT - 20;

        int cx   = spawnMinX + random.nextInt(spawnMaxX - spawnMinX + 1);
        int cy   = spawnMinY + random.nextInt(spawnMaxY - spawnMinY + 1);
        int type = random.nextInt(3) + 1; // 1=size, 2=speed, 3=slow

        currentPowerup = new Powerup(cx, cy, owner, type, System.currentTimeMillis());
        add(currentPowerup);
        // place the powerup just above the rink in the z-order so it appears on the ice
        getContentPane().setComponentZOrder(currentPowerup, getContentPane().getComponentCount() - 2);
    }

    /**
     * manages the full powerup lifecycle each frame: spawning, expiry, collection, and effect revert
     * pre:  playerPaddle, opponentPaddle, and rink all exist
     * post: grow effects that have expired are reverted; a new powerup spawns after the cooldown;
     *       a powerup that times out is removed; only the owner paddle can collect it
     */
    private void handlePowerup() {
        long now = System.currentTimeMillis();

        // revert size effects
        if (player1Grown && now - player1GrowStart >= Powerup.EFFECT_MS) {
            playerPaddle.revert();
            player1Grown = false;
        }
        if (player2Grown && now - player2GrowStart >= Powerup.EFFECT_MS) {
            opponentPaddle.revert();
            player2Grown = false;
        }

        // revert speed effects
        if (playerPaddleSpeedy && now - playerSpeedyStart >= Powerup.EFFECT_MS) {
            playerPaddle.revertSpeed();
            playerPaddleSpeedy = false;
        }
        if (opponentPaddleSpeedy && now - opponentSpeedyStart >= Powerup.EFFECT_MS) {
            opponentPaddle.revertSpeed();
            opponentPaddleSpeedy = false;
        }

        // revert slow effects
        if (playerPaddleSlowed && now - playerSlowedStart >= Powerup.EFFECT_MS) {
            playerPaddle.revertSpeed();
            playerPaddleSlowed = false;
        }
        if (opponentPaddleSlowed && now - opponentSlowedStart >= Powerup.EFFECT_MS) {
            opponentPaddle.revertSpeed();
            opponentPaddleSlowed = false;
        }

        // spawn a new powerup once the cooldown has passed and none is on the field
        if (currentPowerup == null) {
            if (now - lastPowerupEndTime >= Powerup.RESPAWN_MS) {
                spawnPowerup();
            }
            return;
        }

        // remove the powerup if it ran out of field time without being collected
        currentPowerup.checkExpiry(now);
        if (currentPowerup.isActive() == false && currentPowerup.isCollected() == false) {
            remove(currentPowerup);
            lastPowerupEndTime = now;
            currentPowerup = null;
            return;
        }

        int owner = currentPowerup.getOwnerPlayer();
        boolean collectedByOwner = false;

        if (owner == 1 && playerPaddle.collides(currentPowerup)) {
            collectedByOwner = true;
        }
        if (owner == 2 && opponentPaddle.collides(currentPowerup)) {
            collectedByOwner = true;
        }

        if (collectedByOwner) {
            int type  = currentPowerup.getType();

            // retro chime when a player scoops up a powerup
            SoundEffects.play("powerup");

            currentPowerup.collect();
            remove(currentPowerup);
            lastPowerupEndTime = now;
            currentPowerup     = null;

            // ownerPaddle benefits; targetPaddle is the opponent for slow effects
            Paddle ownerPaddle  = (owner == 1) ? playerPaddle   : opponentPaddle;
            Paddle targetPaddle = (owner == 1) ? opponentPaddle : playerPaddle;

            if (type == Powerup.TYPE_SIZE) {
                ownerPaddle.grow();
                if (owner == 1) { player1Grown = true; player1GrowStart = now; }
                else            { player2Grown = true; player2GrowStart = now; }

            } else if (type == Powerup.TYPE_SPEED) {
                ownerPaddle.speedUp();
                if (owner == 1) {
                    playerPaddleSpeedy = true;  playerSpeedyStart  = now;
                    playerPaddleSlowed = false; // cancel any slow on the same paddle
                } else {
                    opponentPaddleSpeedy = true;  opponentSpeedyStart  = now;
                    opponentPaddleSlowed = false;
                }

            } else if (type == Powerup.TYPE_SLOW) {
                targetPaddle.slowDown();
                if (owner == 1) {
                    opponentPaddleSlowed = true;  opponentSlowedStart  = now;
                    opponentPaddleSpeedy = false; // cancel any speed on the same paddle
                } else {
                    playerPaddleSlowed = true;  playerSlowedStart  = now;
                    playerPaddleSpeedy = false;
                }
            }
        }
    }

    /**
     * bounces pucks when they touch a paddle
     * pre:  pucks and both paddles exist
     * post: any puck that hits a paddle moves out, reverses direction, and speeds up slightly
     */
    /**
     * plays the puck-hit sound, louder for faster (harder) hits
     * pre:  puck has just been struck this frame
     * post: the retro hit blip plays at a volume scaled by the puck's speed
     */
    private void playHitSound(Puck puck) {
        float volume = (float) (0.45 + 0.55 * puck.getSpeedFraction());
        SoundEffects.play("puck-hit", volume);
    }

    private void handlePaddleCollisions() {
        for (int i = 0; i < pucks.size(); i++) {
            Puck puck = pucks.get(i);
            if (paddleHitsPuck(playerPaddle, puck)) {
                puck.hitByPaddle(playerPaddle);
                playerPaddle.flashHit();
                playHitSound(puck);
            } else if (paddleHitsPuck(opponentPaddle, puck)) {
                puck.hitByPaddle(opponentPaddle);
                opponentPaddle.flashHit();
                playHitSound(puck);
            }
        }
    }

    /**
     * checks whether a paddle touched a puck this frame, including fast swings
     * pre:  paddle and puck exist and have already moved this frame
     * post: returns true if they overlap now, or if the paddle swept across the
     *       puck during its movement this frame (so a fast swing can't tunnel past it)
     */
    private boolean paddleHitsPuck(Paddle paddle, Puck puck) {
        if (puck.collides(paddle)) {
            return true;
        }

        // rebuild the box the paddle swept through this frame, from its previous
        // position (current minus this frame's velocity) to its current position
        int prevX = paddle.getX() - paddle.getVelocityX();
        int prevY = paddle.getY() - paddle.getVelocityY();
        int boxLeft   = Math.min(paddle.getX(), prevX);
        int boxTop    = Math.min(paddle.getY(), prevY);
        int boxRight  = Math.max(paddle.getX(), prevX) + paddle.getWidth();
        int boxBottom = Math.max(paddle.getY(), prevY) + paddle.getHeight();

        int puckLeft   = puck.getCenterX() - puck.getRadius();
        int puckTop    = puck.getCenterY() - puck.getRadius();
        int puckRight  = puck.getCenterX() + puck.getRadius();
        int puckBottom = puck.getCenterY() + puck.getRadius();

        boolean overlapX = boxLeft < puckRight && puckLeft < boxRight;
        boolean overlapY = boxTop < puckBottom && puckTop < boxBottom;
        return overlapX && overlapY;
    }
}
