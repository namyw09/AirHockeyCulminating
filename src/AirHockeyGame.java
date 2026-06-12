import java.awt.Color;
import java.awt.Frame;
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

// Brighton ng + Youngwoo nam - ICS3U culminating
// main game class
public class AirHockeyGame extends Game {

    // base rink dimensions for the original 800x600 layout
    private static final int BASE_RINK_WIDTH  = 700;
    private static final int BASE_RINK_HEIGHT = 440;
    private static final int BASE_GOAL_HEIGHT = 150;

    // live rink geometry - recomputed in setup() so the rink and everything inside
    // it scales to fill the full-screen window instead of sitting tiny in a corner
    private int rinkX      = 50;
    private int rinkY      = 80;
    private int rinkWidth  = BASE_RINK_WIDTH;
    private int rinkHeight = BASE_RINK_HEIGHT;
    private int goalHeight = BASE_GOAL_HEIGHT;
    private double scale   = 1.0;   // how much bigger than the base layout we are

    // edges/center derived from the rink rect above - computed once in setup()
    // so we don't recompute the same math all over the place
    private int rinkRight;    // x of the right wall (rinkX + rinkWidth)
    private int rinkBottom;   // y of the bottom wall (rinkY + rinkHeight)
    private int rinkCenterX;  // x of the center line
    private int rinkCenterY;  // y of the vertical center
    private int goalTop;      // y where the goal opening starts
    private int goalBottom;   // y where the goal opening ends
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

    private boolean matchMusicStarted  = false;  // true once the main match loop has begun

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

    // set from the name prompt in setup() before play begins
    private String player1Name;
    private String player2Name;

    // candy battle minigame state
    private int     candyBattleTriggerRemaining = 0;     // fires when this many seconds remain
    private boolean candyBattleDone             = false; // only happens once per match
    private int     cursorControlPlayer         = 0;     // 0 = none, 1/2 = candy winner uses mouse
    private int     cursorX                     = 0;     // live mouse position; centered in setup()
    private int     cursorY                     = 0;

    /**
     * sets up the window, timer, player names, and starting objects
     * pre:  the game frame exists but nothing has been added yet
     * post: the rink, starting puck, paddles, and 0-0 scoreboard are ready
     */
    public void setup() {
        // fill the whole screen instead of a small fixed window
        // we had a small window initialized in the top left for our milestone, we realized it was way too small so we made the game full screen
        setExtendedState(Frame.MAXIMIZED_BOTH);
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

        player1Name = promptForName("Enter Player 1's name:");
        player2Name = promptForName("Enter Player 2's name:");

        // now that the window is maximized, stretch the rink to fill the screen,
        int windowWidth  = currentWindowWidth();
        int windowHeight = currentWindowHeight();

        //sideMargin — empty space on tshe left and right
        // headerHeight — the strip up top for the scoreboard, timer, and the W/A/S/D / Arrows control labels
        // bottomMargin — a little breathing room beneath the rink
        int sideMargin   = windowWidth / 25;
        int headerHeight = windowHeight / 8;
        int bottomMargin = windowHeight / 20;
        rinkX      = sideMargin;
        rinkY      = headerHeight;
        rinkWidth  = windowWidth - sideMargin * 2;
        rinkHeight = windowHeight - headerHeight - bottomMargin;
        goalHeight = rinkHeight * BASE_GOAL_HEIGHT / BASE_RINK_HEIGHT;
        // everything inside the rink (paddles, puck, powerups) scales by this
        scale      = (double) rinkHeight / BASE_RINK_HEIGHT;

        // edges/center, derived once so other methods don't redo this math
        rinkRight   = rinkX + rinkWidth;
        rinkBottom  = rinkY + rinkHeight;
        rinkCenterX = rinkX + rinkWidth / 2;
        rinkCenterY = rinkY + rinkHeight / 2;
        goalTop     = rinkY + (rinkHeight - goalHeight) / 2;
        goalBottom  = goalTop + goalHeight;

        cursorX = rinkCenterX;
        cursorY = rinkCenterY;

        // puck and paddles are added before the rink
        addPuck(1, false); // Create the first puck, but don't serve or launch it yet because the game hasn't started

        int paddleInset = (int) Math.round(80 * scale);
        playerPaddle = new CursorControlledPaddle(
                rinkX + paddleInset,
                rinkCenterY,
                new Color(54, 124, 230), scale);

        opponentPaddle = new CursorControlledPaddle(
                rinkRight - paddleInset,
                rinkCenterY,
                new Color(220, 70, 60), scale);

        rink = new Rink(windowWidth, windowHeight,
                rinkX, rinkY, rinkWidth, rinkHeight, goalHeight,
                player1Name, player2Name);

        updateScoreboard();

        add(playerPaddle);
        add(opponentPaddle);
        add(rink);
        rink.setCenterMessage("3");

        lastPowerupEndTime = System.currentTimeMillis();

        // candy battle fires once, at a random moment between the 15-30 seconds left on the clock
        candyBattleTriggerRemaining = 15 + random.nextInt(16);

        PauseButton pauseBtn = new PauseButton(windowWidth - 110, 8, () -> showPauseDialog());
        add(pauseBtn);
        getContentPane().setComponentZOrder(pauseBtn, 0);

        // TODO for youngwoo: explain this above thingy 
    }

    /**
     * the usable width of the game window
     * pre:  the window has been maximized
     * post: returns the content pane width
     */
    private int currentWindowWidth() {
        return getContentPane().getWidth();
    }

    /**
     * the usable height of the game window
     * pre:  the window has been maximized
     * post: returns the content pane height
     */
    private int currentWindowHeight() {
        return getContentPane().getHeight();
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

        // regulation time ran out: wrap up the match
        if (isTimeUp()) {
            handleTimeUp();
            return;
        }

        if (!countdownDone) {
            handleStartCountdown();
            return;
        }

        // surprise candy battle once the clock drops into the trigger window
        if (!candyBattleDone
                && getTimeRemainingSeconds() <= candyBattleTriggerRemaining) {
            candyBattleDone = true;
            startCandyBattle();
            return; // game is paused now; skip the rest of this frame
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

        // keep the clock frozen at full time during the 3-2-1-GO part. at first the timer started counting during the countdown, so we had to pause it until play actually starts

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
     * decides what happens when regulation time runs out
     * pre:  the match clock has reached zero
     * post: a buzzer plays and the match ends - whoever has more goals wins, or
     *       it's a tie if the scores are level
     */
    private void handleTimeUp() {
        SoundEffects.play("buzzer");
        rink.setCenterMessage("");
        finishGame("Time's Up");
    }

    /**
     * asks for a player name until one is entered
     * pre:  message is a valid prompt string
     * post: returns a non-empty name; loops until the player types one
     */
    private String promptForName(String message) {
        String name = "";



        // When we were giving out the game for Michael, Raymond and other people to test, we found that a lot of them don't type in their username. When the computer writes to the match history file, there's no name. We made a while loop to ensure that all players have their username, and we also normalized the null to an empty string so that it doesn't break the code or the system. We loop until the user gives us a valid name 
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
     * pre:  the rink has been created in setup()
     * post: the scoreboard matches the game
     */
    private void updateScoreboard() {
        rink.setScoreboard(player1Score, player2Score, getFormattedTimeRemaining());
    }

    /**
     * shows the pause menu and handles the selected action
     * pre:  game is running
     * post: game is paused and a dialog with Continue / Test Candy Battle / Quit
     *       is shown; Continue resumes, Test Candy Battle starts the camera
     *       minigame now, and Quit returns to the home screen with menu music
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
            AirHockeyApp.showHome();
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

        // run the camera/YOLO stuff on its own thread so the whole game doesn't
        // freeze while the camera is doing its thing
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
            int winner = CandyBattle.run(player1Name, player2Name);
            BattleSoundPlayer.stop();

            SwingUtilities.invokeLater(() -> {
                if (winner == 1 || winner == 2) {
                    String name;
                    if (winner == 1) {
                        name = player1Name;
                    } else {
                        name = player2Name;
                    }
                    JOptionPane.showMessageDialog(this,
                            name + " held the right candy and wins a special powerup!",
                            "Candy Battle Result", JOptionPane.INFORMATION_MESSAGE);
                    applySpecialPowerup(winner);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "No winner this round - no special powerup awarded.",
                            "Candy Battle Result", JOptionPane.INFORMATION_MESSAGE);
                }

                // swap to the hyped-up track for the rest of the match now that
                // the candy battle is over
                startPostCandyMusic();

                resumeGame();
            });
        });
        battleThread.setDaemon(true);
        battleThread.start();
    }

    /**
     * starts the post-candy-battle music loop
     * pre:  the candy battle just finished
     * post: the old sudden-death track now loops as the closing-stretch music;
     *       if the file is missing the current music just keeps playing
     */
    private void startPostCandyMusic() {
        File music = MusicPlayer.findAudio("sudden-death.mp3");
        if (music != null && music.exists()) {
            MusicPlayer.startLoop(music);
        }
    }

    /**
     * gives the candy battle winner cursor control
     * grants the candy battle's special powerup to the winning player
     * pre:  player is 1 or 2
     * post: winner controls their paddle with the mouse for the rest of the match
     */
    private void applySpecialPowerup(int player) {
        cursorControlPlayer = player;

        String name;
        if (player == 1) {
            name = player1Name;
        } else {
            name = player2Name;
        }

        JOptionPane.showMessageDialog(this,
                name + " unlocked cursor control for the rest of the match!",
                "Secret Powerup", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * ends the match and shows who won
     * pre:  time is up or someone hit the score limit
     * post: the game loop stops and the final result pops up on screen
     */
    private void finishGame(String reason) {
        gameOver = true;
        stopGame();
        updateScoreboard();

            // figure out who actually won - and don't forget a tie is possible too

        String result;
        if (player1Score > player2Score) {
            result = player1Name + " wins!";
        } else if (player2Score > player1Score) {
            result = player2Name + " wins!";
        } else {
            result = "Tie game!";
        }

        // pop up the final score so everyone can see how it ended
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
            // saving history is just a bonus, so if it fails to write we just shrug it off
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
                    rinkX, rinkCenterX,
                    rinkY, rinkBottom);
        } else {
            playerPaddle.move(
                    WKeyPressed(), SKeyPressed(), AKeyPressed(), DKeyPressed(),
                    rinkX, rinkCenterX,
                    rinkY, rinkBottom);
        }

        if (cursorControlPlayer == 2) {
            opponentPaddle.followCursor(
                    cursorX, cursorY,
                    rinkCenterX, rinkRight,
                    rinkY, rinkBottom);
        } else {
            opponentPaddle.move(
                    UpKeyPressed(), DownKeyPressed(), LeftKeyPressed(), RightKeyPressed(),
                    rinkCenterX, rinkRight,
                    rinkY, rinkBottom);
        }
    }

    /**
     * adds a puck at center and optionally serves it right away
     * pre:  rink dimensions are initialized
     * post: a new puck is added above the rink layer and included in puck updates
     */
    private void addPuck(int direction, boolean serveNow) {
        Puck newPuck = new Puck(
                rinkCenterX,
                rinkCenterY, scale);

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
     * serves the starting puck once the opening countdown ends
     * pre:  the starting puck exists and hasn't been served yet
     * post: the starting puck gets a randomized diagonal serve toward Player 2
     */
    private void serveAllPucks() {
        pucks.get(0).serve(1, random);
    }

    /**
     * adds extra pucks as the match clock reaches ramp moments
     * pre:  countdown is done and timer is running
     * post: second puck appears at 40 seconds, third puck appears at 20 seconds
     */
    private void addTimedExtraPucks() {
        int remaining = getTimeRemainingSeconds();

        secondPuckAdded = tryAddTimedPuck(secondPuckAdded, remaining, 40, -1);
        thirdPuckAdded = tryAddTimedPuck(thirdPuckAdded, remaining, 20, 1);
    }

    /**
     * adds one extra puck when the clock reaches its spawn time
     * pre:  remaining is the match time left, spawnTime is the time to add the puck
     * post: returns true once this puck has been added
     */
    private boolean tryAddTimedPuck(boolean alreadyAdded, int remaining, int spawnTime, int direction) {
        if (alreadyAdded || remaining > spawnTime) {
            return alreadyAdded;
        }

        addPuck(direction, true);
        return true;
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
        int puckDiameter = puck.getRadius() * 2;

        boolean inGoalOpening = false;
        if (puck.getCenterY() >= goalTop && puck.getCenterY() <= goalBottom) {
            inGoalOpening = true;
        }

        boolean bounced = false;

        // top wall
        if (puck.getY() <= rinkY) {
            puck.setPuckY(rinkY);
            puck.bounceVertical();
            bounced = true;
        }

        // bottom wall
        if (puck.getY() + puckDiameter >= rinkBottom) {
            puck.setPuckY(rinkBottom - puckDiameter);
            puck.bounceVertical();
            bounced = true;
        }

        // left and right walls - but don't bounce if the puck is lined up with the goal,
        // otherwise you could never actually score
        if (puck.getX() <= rinkX && inGoalOpening == false) {
            puck.setPuckX(rinkX);
            puck.bounceHorizontal();
            bounced = true;
        }

        if (puck.getX() + puckDiameter >= rinkRight && inGoalOpening == false) {
            puck.setPuckX(rinkRight - puckDiameter);
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
        int puckDiameter = puck.getRadius() * 2;

        boolean inGoalOpening = false;
        if (puck.getCenterY() >= goalTop && puck.getCenterY() <= goalBottom) {
            inGoalOpening = true;
        }

        if (inGoalOpening == false) {
            return;
        }

        // puck crossed the left goal line
        if (puck.getX() + puckDiameter < rinkX) {
            player2Score++;
            onGoalScored();
            if (gameOver) {
                return;
            }
            puck.reset(rinkCenterX, rinkCenterY, -1, random);
        }

        // puck crossed the right goal line
        if (puck.getX() > rinkRight) {
            player1Score++;
            onGoalScored();
            if (gameOver) {
                return;
            }
            puck.reset(rinkCenterX, rinkCenterY, 1, random);
        }
    }

    /**
     * shared reaction to any goal being scored
     * pre:  a score was just incremented
     * post: the goal sound plays and the scoreboard updates; the match ends if
     *       this goal reaches the score limit
     */
    private void onGoalScored() {
        SoundEffects.play("goal");
        updateScoreboard();
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
        int half = random.nextInt(2);
        int owner;
        int spawnMinX;
        int spawnMaxX;

        if (half == 0) {
            owner     = 1;
            spawnMinX = rinkX + 80;
            spawnMaxX = rinkCenterX - (int) Math.round(Powerup.RADIUS * scale);
        } else {
            owner     = 2;
            spawnMinX = rinkCenterX + (int) Math.round(Powerup.RADIUS * scale);
            spawnMaxX = rinkRight - 80;
        }

        int spawnMinY = rinkY + 20;
        int spawnMaxY = rinkBottom - 20;

        int cx   = spawnMinX + random.nextInt(spawnMaxX - spawnMinX + 1);
        int cy   = spawnMinY + random.nextInt(spawnMaxY - spawnMinY + 1);
        int type = random.nextInt(3) + 1; // 1=size, 2=speed, 3=slow

        currentPowerup = new Powerup(cx, cy, owner, type, System.currentTimeMillis(), scale);
        add(currentPowerup);
        // drawing order was super confusing at first - this shoves the powerup just
        // above the rink so it shows up on the ice but still under the puck and paddles
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
                playHitSound(puck);
            } else if (paddleHitsPuck(opponentPaddle, puck)) {
                puck.hitByPaddle(opponentPaddle);
                playHitSound(puck);
            }
        }
    }

    /**
     * checks if a paddle touched the puck this frame, even on really fast swings
     * pre:  paddle and puck exist and have already moved this frame
     * post: returns true if they overlap right now, OR if the paddle swept over the
     *       puck during this frame. we had a bug where swinging fast made the paddle
     *       teleport straight through the puck, so this catches that too
     */
    private boolean paddleHitsPuck(Paddle paddle, Puck puck) {
        if (puck.collides(paddle)) {
            return true;
        }

        // build the box the paddle swept through this frame - basically from where it
        // was last frame (current minus velocity) to where it is now
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
