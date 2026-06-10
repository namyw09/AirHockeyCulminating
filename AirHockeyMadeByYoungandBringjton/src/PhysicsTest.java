import java.awt.Color;

// plain unit tests for the physics changes - no JUnit needed
// run with: java -ea -cp bin PhysicsTest
public class PhysicsTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testFrictionSlowsPuck();
        testSpeedCapOnHardHit();
        testPaddleVelocityTransfersToPuck();
        testPaddleAccelerates();
        testPaddleDecelerates();

        System.out.println("\n" + passed + " passed, " + failed + " failed.");
        if (failed > 0) {
            System.exit(1);
        }
    }

    // pre:  none
    // post: confirms that calling update() 60 times reduces puck speed due to friction
    private static void testFrictionSlowsPuck() {
        Puck puck = new Puck(400, 300);
        float initialX = puck.getXSpeed();

        for (int i = 0; i < 60; i++) {
            puck.update();
        }

        check("Friction reduces xSpeed after 60 frames",
                puck.getXSpeed() < initialX);
    }

    // pre:  none
    // post: confirms that xSpeed after hitByPaddle never exceeds MAX_SPEED (18)
    private static void testSpeedCapOnHardHit() {
        Puck   puck   = new Puck(400, 300);
        Paddle paddle = new Paddle(350, 300, Color.BLUE);

        // simulate paddle moving hard to the right for many frames
        for (int i = 0; i < 20; i++) {
            paddle.move(false, false, false, true, 50, 450, 80, 520);
        }

        puck.hitByPaddle(paddle);

        check("xSpeed does not exceed MAX_SPEED after hard hit",
                Math.abs(puck.getXSpeed()) <= 18f);
        check("ySpeed does not exceed MAX_SPEED after hard hit",
                Math.abs(puck.getYSpeed()) <= 18f);
    }

    // pre:  none
    // post: confirms that a paddle moving right into a left-moving puck produces
    //       higher outgoing speed than a stationary paddle does
    private static void testPaddleVelocityTransfersToPuck() {
        Puck movingPaddleHit    = new Puck(400, 300);
        Puck stationaryPaddleHit = new Puck(400, 300);

        // puck moves left toward the player-1 paddle
        movingPaddleHit.xSpeed    = -9f;
        stationaryPaddleHit.xSpeed = -9f;

        Paddle movingPaddle     = new Paddle(350, 300, Color.BLUE);
        Paddle stationaryPaddle = new Paddle(350, 300, Color.BLUE);

        // accelerate one paddle to the right (into the puck)
        for (int i = 0; i < 10; i++) {
            movingPaddle.move(false, false, false, true, 50, 450, 80, 520);
        }

        movingPaddleHit.hitByPaddle(movingPaddle);
        stationaryPaddleHit.hitByPaddle(stationaryPaddle);

        check("Moving paddle hit produces higher puck speed than stationary hit",
                Math.abs(movingPaddleHit.getXSpeed()) > Math.abs(stationaryPaddleHit.getXSpeed()));
    }

    // pre:  none
    // post: confirms that paddle velocity increases each frame while a key is held
    private static void testPaddleAccelerates() {
        Paddle paddle = new Paddle(200, 300, Color.RED);

        int prevVel = paddle.getVelocityX();
        boolean accelerated = false;

        for (int i = 0; i < 10; i++) {
            paddle.move(false, false, false, true, 50, 450, 80, 520);
            int newVel = paddle.getVelocityX();
            if (newVel > prevVel) {
                accelerated = true;
            }
            prevVel = newVel;
        }

        check("Paddle velocity increases while right key is held", accelerated);
    }

    // pre:  none
    // post: confirms that paddle velocity decreases each frame after key is released
    private static void testPaddleDecelerates() {
        Paddle paddle = new Paddle(200, 300, Color.RED);

        // build up speed
        for (int i = 0; i < 10; i++) {
            paddle.move(false, false, false, true, 50, 450, 80, 520);
        }

        int peakVel = paddle.getVelocityX();

        // release all keys
        paddle.move(false, false, false, false, 50, 450, 80, 520);
        int afterOneFrame = paddle.getVelocityX();

        check("Paddle velocity decreases after key release",
                afterOneFrame < peakVel);
    }

    // post: prints PASS or FAIL and updates counters
    private static void check(String label, boolean condition) {
        if (condition) {
            System.out.println("PASS  " + label);
            passed++;
        } else {
            System.out.println("FAIL  " + label);
            failed++;
        }
    }
}
