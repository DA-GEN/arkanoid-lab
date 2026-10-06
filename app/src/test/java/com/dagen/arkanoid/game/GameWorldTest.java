package com.dagen.arkanoid.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class GameWorldTest {
    private static final float DT = 0.02f;
    private static final float EPS = 1e-3f;

    private GameWorld world;
    private RecordingListener listener;

    @Before
    public void setUp() {
        world = new GameWorld(Difficulty.NORMAL);
        listener = new RecordingListener();
        world.setListener(listener);
    }

    @Test
    public void newGameWaitsWithBallOnPaddle() {
        assertEquals(GameState.READY, world.getState());
        assertEquals(1, world.getLevel());
        assertEquals(Difficulty.NORMAL.getLives(), world.getLives());
        assertEquals(world.getPaddle().getCenterX(), world.getBall().getX(), EPS);
        assertTrue(world.getBall().getY() < world.getPaddle().getTop());
    }

    @Test
    public void ballFollowsPaddleBeforeLaunch() {
        world.movePaddle(200f);
        assertEquals(200f, world.getBall().getX(), EPS);
    }

    @Test
    public void tapLaunchesBallUpwards() {
        world.tap();
        assertEquals(GameState.RUNNING, world.getState());
        assertTrue(world.getBall().getVy() < 0f);
        assertEquals(Difficulty.NORMAL.getBallSpeed(), world.getBall().getSpeed(), 0.5f);
    }

    @Test
    public void paddleStaysInsideField() {
        world.movePaddle(-1000f);
        assertEquals(0f, world.getPaddle().getLeft(), EPS);
        world.movePaddle(5000f);
        assertEquals(GameWorld.WIDTH, world.getPaddle().getRight(), EPS);
    }

    @Test
    public void ballBouncesOffSideWall() {
        world.tap();
        Ball ball = world.getBall();
        ball.setPosition(GameWorld.BALL_RADIUS + 2f, 1000f);
        ball.setVelocity(-500f, 100f);
        world.update(DT);
        assertTrue(ball.getVx() > 0f);
        assertTrue(ball.getX() >= ball.getRadius());
    }

    @Test
    public void ballBouncesOffTopBorder() {
        world.tap();
        Ball ball = world.getBall();
        ball.setPosition(450f, GameWorld.HUD_HEIGHT + GameWorld.BALL_RADIUS + 2f);
        ball.setVelocity(0f, -500f);
        world.update(DT);
        assertTrue(ball.getVy() > 0f);
    }

    @Test
    public void paddleHitAngleDependsOnHitPoint() {
        world.tap();
        Paddle paddle = world.getPaddle();
        Ball ball = world.getBall();
        ball.setPosition(paddle.getCenterX() + paddle.getWidth() / 3f, paddle.getTop() - ball.getRadius() - 2f);
        ball.setVelocity(0f, 500f);
        world.update(DT);
        assertTrue(ball.getVy() < 0f);
        assertTrue(ball.getVx() > 0f);
    }

    @Test
    public void hittingBrickAddsScoreAndReflectsBall() {
        world.tap();
        Brick target = clearTarget();
        aimAt(target);
        world.update(DT);
        assertEquals(GameWorld.POINTS_PER_HIT * Difficulty.NORMAL.getScoreMultiplier(), world.getScore());
        assertFalse(target.isAlive());
        assertTrue(world.getBall().getVy() > 0f);
    }

    @Test
    public void ballSpeedsUpAfterBrickHit() {
        world.tap();
        aimAt(clearTarget());
        world.update(DT);
        assertTrue(world.getBall().getSpeed() > 400f);
    }

    @Test
    public void unbreakableBricksGiveNoPoints() {
        world = new GameWorld(Difficulty.NORMAL, 2);
        world.tap();
        Brick wall = null;
        for (Brick brick : world.getBricks()) {
            if (brick.isUnbreakable()) {
                wall = brick;
            }
        }
        assertNotNull(wall);
        aimAt(wall);
        world.update(DT);
        assertEquals(0, world.getScore());
        assertTrue(wall.isAlive());
    }

    @Test
    public void hardModeBricksNeedExtraHit() {
        GameWorld hard = new GameWorld(Difficulty.HARD);
        for (Brick brick : hard.getBricks()) {
            assertEquals(2, brick.getMaxHits());
        }
    }

    @Test
    public void lostBallTakesLifeAndResetsBall() {
        world.tap();
        dropBall();
        assertEquals(Difficulty.NORMAL.getLives() - 1, world.getLives());
        assertEquals(GameState.READY, world.getState());
        assertEquals(world.getLives(), listener.livesLeft);
    }

    @Test
    public void gameOverWhenAllLivesAreLost() {
        for (int i = 0; i < Difficulty.NORMAL.getLives(); i++) {
            world.tap();
            dropBall();
        }
        assertEquals(GameState.GAME_OVER, world.getState());
        assertEquals(0, world.getLives());
        assertEquals(Boolean.FALSE, listener.victory);
    }

    @Test
    public void clearingLevelMovesToNextOne() {
        world.tap();
        Brick target = clearTarget();
        destroyAllExcept(target);
        aimAt(target);
        world.update(DT);
        assertEquals(GameState.LEVEL_CLEARED, world.getState());
        assertEquals(1, listener.clearedLevel);

        world.tap();
        assertEquals(GameState.READY, world.getState());
        assertEquals(2, world.getLevel());
    }

    @Test
    public void clearingLastLevelIsVictory() {
        world = new GameWorld(Difficulty.NORMAL, Levels.count() - 1);
        world.setListener(listener);
        world.tap();
        Brick target = clearTarget();
        destroyAllExcept(target);
        aimAt(target);
        world.update(DT);
        assertEquals(GameState.VICTORY, world.getState());
        assertEquals(Boolean.TRUE, listener.victory);
    }

    @Test
    public void pausedWorldDoesNotMove() {
        world.tap();
        world.pause();
        float y = world.getBall().getY();
        world.update(DT);
        assertEquals(y, world.getBall().getY(), EPS);

        world.togglePause();
        assertEquals(GameState.RUNNING, world.getState());
    }

    @Test
    public void tapAfterGameOverRestarts() {
        for (int i = 0; i < Difficulty.NORMAL.getLives(); i++) {
            world.tap();
            dropBall();
        }
        world.tap();
        assertEquals(GameState.READY, world.getState());
        assertEquals(Difficulty.NORMAL.getLives(), world.getLives());
        assertEquals(0, world.getScore());
        assertEquals(1, world.getLevel());
    }

    private void dropBall() {
        Ball ball = world.getBall();
        ball.setPosition(100f, GameWorld.HEIGHT + 2 * ball.getRadius());
        ball.setVelocity(0f, 300f);
        world.update(DT);
    }

    private void aimAt(Brick brick) {
        Ball ball = world.getBall();
        ball.setPosition((brick.getLeft() + brick.getRight()) / 2f, brick.getBottom() + ball.getRadius() + 2f);
        ball.setVelocity(0f, -400f);
    }

    /** A breakable brick with nothing alive below it. */
    private Brick clearTarget() {
        for (Brick candidate : world.getBricks()) {
            if (candidate.isUnbreakable()) {
                continue;
            }
            boolean blocked = false;
            for (Brick other : world.getBricks()) {
                if (other != candidate && other.isAlive()
                        && other.getLeft() == candidate.getLeft() && other.getTop() > candidate.getTop()) {
                    blocked = true;
                    break;
                }
            }
            if (!blocked) {
                return candidate;
            }
        }
        throw new AssertionError("no reachable brick");
    }

    private void destroyAllExcept(Brick target) {
        for (Brick brick : world.getBricks()) {
            int keep = brick == target ? 1 : 0;
            while (!brick.isUnbreakable() && brick.getHitsLeft() > keep) {
                brick.hit();
            }
        }
        assertEquals(1, world.remainingBricks());
    }

    private static final class RecordingListener implements GameWorld.Listener {
        int livesLeft = -1;
        int clearedLevel = -1;
        Boolean victory;

        @Override
        public void onLifeLost(int livesLeft) {
            this.livesLeft = livesLeft;
        }

        @Override
        public void onLevelCleared(int level) {
            clearedLevel = level;
        }

        @Override
        public void onGameFinished(int score, boolean victory) {
            this.victory = victory;
        }
    }
}
