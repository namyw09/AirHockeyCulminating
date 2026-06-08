import java.awt.Color;

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

    private Rink rink;
    private Paddle playerPaddle;
    private Paddle opponentPaddle;
    private Puck puck;

    // current score for each player
    private int player1Score = 0;
    private int player2Score = 0;
    private boolean gameOver = false;

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
    }

    /**
     * runs one frame of the game
     * pre:  setup() is done and all game objects exist
     * post: movement, goals, and collisions are checked, or the game ends if time is up
     */
    public void act() {
        updateScoreboard();

        if (gameOver) {
            return;
        }

        if (isTimeUp()) {
            finishGame();
            return;
        }

        handleInput();
        puck.update();
        handleGoals();
        handleWallCollisions();
        handlePaddleCollisions();
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
     * pre:  time is up and the game has not ended already
     * post: the game loop stops and the final result pops up
     */
    private void finishGame() {
        gameOver = true;
        stopGame();
        updateScoreboard();

        String result;
        if (player1Score > player2Score) {
            result = player1Name + " wins!";
        } else if (player2Score > player1Score) {
            result = player2Name + " wins!";
        } else {
            result = "Tie game!";
        }

        JOptionPane.showMessageDialog(this,
                result + "\nFinal Score: " + player1Name + " " + player1Score
                        + " - " + player2Score + " " + player2Name,
                "Time's Up", JOptionPane.INFORMATION_MESSAGE);
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
            puck.reset(RINK_X + RINK_WIDTH / 2, RINK_Y + RINK_HEIGHT / 2, -1);
        }

        // puck crossed the right goal line
        if (puck.getX() > RINK_X + RINK_WIDTH) {
            player1Score++;
            updateScoreboard();
            puck.reset(RINK_X + RINK_WIDTH / 2, RINK_Y + RINK_HEIGHT / 2, 1);
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
