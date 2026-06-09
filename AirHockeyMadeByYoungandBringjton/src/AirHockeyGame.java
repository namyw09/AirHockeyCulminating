import java.awt.Color;
import java.util.Random;

import javax.swing.JOptionPane;

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
    private static final int MATCH_LENGTH_SECONDS = 90;
    private static final int SCORE_LIMIT = 7;

    private Rink rink;
    private Paddle playerPaddle;
    private Paddle opponentPaddle;
    private Puck puck;

    // current score for each player
    private int player1Score = 0;
    private int player2Score = 0;
    private boolean gameOver = false;

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

    // default names in case the player skips the input
    private String player1Name = "Player 1";
    private String player2Name = "Player 2";

    /**
     * sets up the window, timer, player names, and starting objects
     * pre:  the game frame exists but nothing has been added yet
     * post: the rink, puck, paddles, and 0-0 scoreboard are ready
     */
    public void setup() {
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setTitle("Air Hockey");
        setBackground(new Color(20, 30, 48));
        setDelay(16);
        setMatchLengthSeconds(MATCH_LENGTH_SECONDS);

        // show the controls before anything starts
        JOptionPane.showMessageDialog(this,
                "Player 1 (Blue): W / A / S / D\nPlayer 2 (Red): Arrow Keys\n\nScore by hitting the puck into the other side's goal.",
                "How to Play", JOptionPane.INFORMATION_MESSAGE);

        player1Name = promptForName("Enter Player 1's name:", "Player 1");
        player2Name = promptForName("Enter Player 2's name:", "Player 2");

        // puck and paddles are added before the rink so they stay visible
        puck = new Puck(
                RINK_X + RINK_WIDTH / 2,
                RINK_Y + RINK_HEIGHT / 2);

        playerPaddle = new Paddle(
                RINK_X + 80,
                RINK_Y + RINK_HEIGHT / 2,
                new Color(54, 124, 230));

        opponentPaddle = new Paddle(
                RINK_X + RINK_WIDTH - 80,
                RINK_Y + RINK_HEIGHT / 2,
                new Color(220, 70, 60));

        rink = new Rink(WINDOW_WIDTH, WINDOW_HEIGHT,
                RINK_X, RINK_Y, RINK_WIDTH, RINK_HEIGHT, GOAL_HEIGHT,
                player1Name, player2Name);

        updateScoreboard();

        add(puck);
        add(playerPaddle);
        add(opponentPaddle);
        add(rink);

        lastPowerupEndTime = System.currentTimeMillis();
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

        if (isTimeUp()) {
            finishGame("Time's Up");
            return;
        }

        handleInput();
        puck.update();
        handleGoals();
        if (gameOver) {
            return;
        }
        handleWallCollisions();
        handlePaddleCollisions();
        handlePowerup();
    }

    /**
     * asks for a player name and uses the default if nothing is typed
     * pre:  the prompt message and backup name are ready
     * post: a usable player name is returned
     */
    private String promptForName(String message, String defaultName) {
        String name = JOptionPane.showInputDialog(this,
                message, "Player Names", JOptionPane.PLAIN_MESSAGE);

        if (name == null || name.equals("")) {
            return defaultName;
        }

        return name;
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
    }

    /**
     * moves both paddles using the keys being held
     * pre:  both paddles exist and Game has the current key states
     * post: each paddle stays inside its side of the rink
     */
    private void handleInput() {
        playerPaddle.move(
                WKeyPressed(), SKeyPressed(), AKeyPressed(), DKeyPressed(),
                RINK_X, RINK_X + RINK_WIDTH / 2,
                RINK_Y, RINK_Y + RINK_HEIGHT);

        opponentPaddle.move(
                UpKeyPressed(), DownKeyPressed(), LeftKeyPressed(), RightKeyPressed(),
                RINK_X + RINK_WIDTH / 2, RINK_X + RINK_WIDTH,
                RINK_Y, RINK_Y + RINK_HEIGHT);
    }

    /**
     * bounces the puck off the rink walls, except where the goals are
     * pre:  the puck exists and already moved this frame
     * post: the puck is pushed back in bounds and bounces if it hit a wall
     */
    private void handleWallCollisions() {
        int goalTop      = RINK_Y + (RINK_HEIGHT - GOAL_HEIGHT) / 2;
        int goalBottom   = goalTop + GOAL_HEIGHT;
        int puckDiameter = puck.getRadius() * 2;

        boolean inGoalOpening = false;
        if (puck.getCenterY() >= goalTop && puck.getCenterY() <= goalBottom) {
            inGoalOpening = true;
        }

        // top wall
        if (puck.getY() <= RINK_Y) {
            puck.setY(RINK_Y);
            puck.bounceVertical();
        }

        // bottom wall
        if (puck.getY() + puckDiameter >= RINK_Y + RINK_HEIGHT) {
            puck.setY(RINK_Y + RINK_HEIGHT - puckDiameter);
            puck.bounceVertical();
        }

        // left and right walls - skip if the puck is lined up with the goal
        if (puck.getX() <= RINK_X && inGoalOpening == false) {
            puck.setX(RINK_X);
            puck.bounceHorizontal();
        }

        if (puck.getX() + puckDiameter >= RINK_X + RINK_WIDTH && inGoalOpening == false) {
            puck.setX(RINK_X + RINK_WIDTH - puckDiameter);
            puck.bounceHorizontal();
        }
    }

    /**
     * updates the score if the puck goes into a goal
     * pre:  the puck exists and might be lined up with a goal
     * post: the right score goes up, the scoreboard updates, and the puck resets
     */
    private void handleGoals() {
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
            updateScoreboard();
            checkScoreLimit();
            if (gameOver) {
                return;
            }
            puck.reset(RINK_X + RINK_WIDTH / 2, RINK_Y + RINK_HEIGHT / 2, -1);
        }

        // puck crossed the right goal line
        if (puck.getX() > RINK_X + RINK_WIDTH) {
            player1Score++;
            updateScoreboard();
            checkScoreLimit();
            if (gameOver) {
                return;
            }
            puck.reset(RINK_X + RINK_WIDTH / 2, RINK_Y + RINK_HEIGHT / 2, 1);
        }
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
     * pre:  puck, playerPaddle, opponentPaddle, and rink all exist
     * post: grow effects that have expired are reverted; a new powerup spawns after the cooldown;
     *       a powerup that times out is removed; a powerup touched by the puck is collected and
     *       the owner's paddle grows
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

        if (puck.collides(currentPowerup)) {
            int owner = currentPowerup.getOwnerPlayer();
            int type  = currentPowerup.getType();

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
     * bounces the puck when it touches a paddle
     * pre:  the puck and both paddles exist
     * post: if the puck hits a paddle, it moves out and reverses direction
     */
    private void handlePaddleCollisions() {
        if (puck.collides(playerPaddle)) {
            puck.hitByPaddle(playerPaddle);
        } else if (puck.collides(opponentPaddle)) {
            puck.hitByPaddle(opponentPaddle);
        }
    }
}
