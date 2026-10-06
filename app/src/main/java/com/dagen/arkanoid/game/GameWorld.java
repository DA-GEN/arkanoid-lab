package com.dagen.arkanoid.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Game logic in fixed world units (WIDTH x HEIGHT). The view scales it to the screen.
 * Public methods are synchronized because touch input and the render thread share the world.
 */
public class GameWorld {
    public static final float WIDTH = 900f;
    public static final float HEIGHT = 1600f;
    public static final float HUD_HEIGHT = 120f;

    static final float BALL_RADIUS = 14f;
    static final float PADDLE_TOP = 1460f;
    static final float PADDLE_HEIGHT = 26f;
    static final float BRICK_TOP = HUD_HEIGHT + 90f;
    static final float BRICK_HEIGHT = 42f;
    static final float BRICK_GAP = 6f;
    static final float SIDE_MARGIN = 20f;
    static final int POINTS_PER_HIT = 10;

    private static final float MAX_BOUNCE_ANGLE = (float) Math.toRadians(60);
    private static final float LAUNCH_ANGLE = (float) Math.toRadians(-20);
    private static final float SPEED_UP = 1.015f;
    private static final float MAX_SPEED_FACTOR = 1.6f;
    private static final float MAX_STEP = BALL_RADIUS / 2f;
    private static final float MAX_FRAME_TIME = 0.05f;

    private static final Logger LOG = Logger.getLogger(GameWorld.class.getName());

    public interface Listener {
        default void onLifeLost(int livesLeft) {
        }

        default void onLevelCleared(int level) {
        }

        default void onGameFinished(int score, boolean victory) {
        }
    }

    private final Difficulty difficulty;
    private final Paddle paddle;
    private final Ball ball = new Ball(BALL_RADIUS);
    private final List<Brick> bricks = new ArrayList<>();
    private Listener listener = new Listener() {
    };

    private GameState state;
    private int levelIndex;
    private int lives;
    private int score;
    private float speed;

    public GameWorld(Difficulty difficulty) {
        this(difficulty, 0);
    }

    GameWorld(Difficulty difficulty, int startLevel) {
        this.difficulty = difficulty;
        this.paddle = new Paddle(WIDTH * difficulty.getPaddleWidthRatio(), PADDLE_HEIGHT, PADDLE_TOP, 0f, WIDTH);
        this.lives = difficulty.getLives();
        startLevel(startLevel);
    }

    public synchronized void setListener(Listener listener) {
        this.listener = listener != null ? listener : new Listener() {
        };
    }

    public synchronized void update(float dt) {
        if (state != GameState.RUNNING || dt <= 0f) {
            return;
        }
        dt = Math.min(dt, MAX_FRAME_TIME);
        int steps = Math.max(1, (int) Math.ceil(speed * dt / MAX_STEP));
        float step = dt / steps;
        for (int i = 0; i < steps && state == GameState.RUNNING; i++) {
            ball.move(step);
            collideWalls();
            if (state != GameState.RUNNING) {
                break;
            }
            collidePaddle();
            collideBricks();
        }
    }

    public synchronized void movePaddle(float x) {
        paddle.moveTo(x);
        if (state == GameState.READY) {
            placeBallOnPaddle();
        }
    }

    /** Single action for a tap: launch, resume, go to the next level or restart. */
    public synchronized void tap() {
        switch (state) {
            case READY:
                launch();
                break;
            case PAUSED:
                resume();
                break;
            case LEVEL_CLEARED:
                startLevel(levelIndex + 1);
                break;
            case GAME_OVER:
            case VICTORY:
                restart();
                break;
            default:
                break;
        }
    }

    public synchronized void pause() {
        if (state == GameState.RUNNING) {
            state = GameState.PAUSED;
            LOG.fine("game paused");
        }
    }

    public synchronized void resume() {
        if (state == GameState.PAUSED) {
            state = GameState.RUNNING;
            LOG.fine("game resumed");
        }
    }

    public synchronized void togglePause() {
        if (state == GameState.PAUSED) {
            resume();
        } else {
            pause();
        }
    }

    public synchronized void restart() {
        LOG.info("restarting game on " + difficulty);
        score = 0;
        lives = difficulty.getLives();
        startLevel(0);
    }

    private void launch() {
        speed = difficulty.getBallSpeed();
        ball.setVelocity((float) Math.sin(LAUNCH_ANGLE) * speed, -(float) Math.cos(LAUNCH_ANGLE) * speed);
        state = GameState.RUNNING;
        LOG.finer("ball launched with speed " + speed);
    }

    private void startLevel(int index) {
        levelIndex = index;
        buildBricks(Levels.layout(index));
        speed = difficulty.getBallSpeed();
        paddle.moveTo(WIDTH / 2f);
        placeBallOnPaddle();
        state = GameState.READY;
        LOG.info(String.format(Locale.ROOT, "level %d started, bricks: %d, difficulty: %s",
                index + 1, remainingBricks(), difficulty));
    }

    private void buildBricks(String[] layout) {
        bricks.clear();
        float width = (WIDTH - 2 * SIDE_MARGIN - (Levels.COLUMNS - 1) * BRICK_GAP) / Levels.COLUMNS;
        for (int row = 0; row < layout.length; row++) {
            String line = layout[row];
            float top = BRICK_TOP + row * (BRICK_HEIGHT + BRICK_GAP);
            for (int col = 0; col < line.length() && col < Levels.COLUMNS; col++) {
                char cell = line.charAt(col);
                float left = SIDE_MARGIN + col * (width + BRICK_GAP);
                if (cell == '#') {
                    bricks.add(Brick.unbreakable(left, top, left + width, top + BRICK_HEIGHT));
                } else if (Character.isDigit(cell)) {
                    int hits = Math.min(Levels.MAX_HITS, (cell - '0') + difficulty.getExtraBrickHits());
                    bricks.add(Brick.breakable(left, top, left + width, top + BRICK_HEIGHT, hits));
                }
            }
        }
    }

    private void placeBallOnPaddle() {
        ball.setVelocity(0f, 0f);
        ball.setPosition(paddle.getCenterX(), paddle.getTop() - BALL_RADIUS - 1f);
    }

    private void collideWalls() {
        float r = ball.getRadius();
        if (ball.getX() - r < 0f) {
            ball.setX(r);
            ball.setVx(Math.abs(ball.getVx()));
        } else if (ball.getX() + r > WIDTH) {
            ball.setX(WIDTH - r);
            ball.setVx(-Math.abs(ball.getVx()));
        }
        if (ball.getY() - r < HUD_HEIGHT) {
            ball.setY(HUD_HEIGHT + r);
            ball.setVy(Math.abs(ball.getVy()));
        } else if (ball.getY() - r > HEIGHT) {
            loseLife();
        }
    }

    private void collidePaddle() {
        if (ball.getVy() <= 0f
                || !intersects(paddle.getLeft(), paddle.getTop(), paddle.getRight(), paddle.getBottom())) {
            return;
        }
        float offset = (ball.getX() - paddle.getCenterX()) / (paddle.getWidth() / 2f);
        offset = Math.max(-1f, Math.min(1f, offset));
        float angle = offset * MAX_BOUNCE_ANGLE;
        ball.setVelocity((float) Math.sin(angle) * speed, -(float) Math.cos(angle) * speed);
        ball.setY(paddle.getTop() - ball.getRadius());
    }

    private void collideBricks() {
        for (Brick brick : bricks) {
            if (brick.isAlive()
                    && intersects(brick.getLeft(), brick.getTop(), brick.getRight(), brick.getBottom())) {
                bounceOff(brick);
                hitBrick(brick);
                return;
            }
        }
    }

    private void bounceOff(Brick brick) {
        float r = ball.getRadius();
        float overlapLeft = ball.getX() + r - brick.getLeft();
        float overlapRight = brick.getRight() - (ball.getX() - r);
        float overlapTop = ball.getY() + r - brick.getTop();
        float overlapBottom = brick.getBottom() - (ball.getY() - r);
        float minX = Math.min(overlapLeft, overlapRight);
        float minY = Math.min(overlapTop, overlapBottom);

        if (minX < minY) {
            if (overlapLeft < overlapRight) {
                ball.setX(brick.getLeft() - r);
                ball.setVx(-Math.abs(ball.getVx()));
            } else {
                ball.setX(brick.getRight() + r);
                ball.setVx(Math.abs(ball.getVx()));
            }
        } else {
            if (overlapTop < overlapBottom) {
                ball.setY(brick.getTop() - r);
                ball.setVy(-Math.abs(ball.getVy()));
            } else {
                ball.setY(brick.getBottom() + r);
                ball.setVy(Math.abs(ball.getVy()));
            }
        }
    }

    private void hitBrick(Brick brick) {
        if (brick.isUnbreakable()) {
            return;
        }
        score += POINTS_PER_HIT * difficulty.getScoreMultiplier();
        if (brick.hit()) {
            LOG.fine("brick destroyed, score: " + score);
        }
        speed = Math.min(speed * SPEED_UP, difficulty.getBallSpeed() * MAX_SPEED_FACTOR);
        ball.setSpeed(speed);

        if (remainingBricks() == 0) {
            finishLevel();
        }
    }

    private void finishLevel() {
        int level = levelIndex + 1;
        if (level >= Levels.count()) {
            state = GameState.VICTORY;
            LOG.info("all levels cleared, final score: " + score);
            listener.onGameFinished(score, true);
        } else {
            state = GameState.LEVEL_CLEARED;
            LOG.info("level " + level + " cleared, score: " + score);
            listener.onLevelCleared(level);
        }
    }

    private void loseLife() {
        lives--;
        if (lives <= 0) {
            lives = 0;
            state = GameState.GAME_OVER;
            LOG.info("game over, final score: " + score);
            listener.onGameFinished(score, false);
        } else {
            speed = difficulty.getBallSpeed();
            placeBallOnPaddle();
            state = GameState.READY;
            LOG.fine("life lost, lives left: " + lives);
            listener.onLifeLost(lives);
        }
    }

    private boolean intersects(float left, float top, float right, float bottom) {
        float nearestX = Math.max(left, Math.min(ball.getX(), right));
        float nearestY = Math.max(top, Math.min(ball.getY(), bottom));
        float dx = ball.getX() - nearestX;
        float dy = ball.getY() - nearestY;
        return dx * dx + dy * dy <= ball.getRadius() * ball.getRadius();
    }

    public synchronized int remainingBricks() {
        int count = 0;
        for (Brick brick : bricks) {
            if (brick.isAlive() && !brick.isUnbreakable()) {
                count++;
            }
        }
        return count;
    }

    public synchronized GameState getState() {
        return state;
    }

    public synchronized int getScore() {
        return score;
    }

    public synchronized int getLives() {
        return lives;
    }

    /** 1-based level number. */
    public synchronized int getLevel() {
        return levelIndex + 1;
    }

    public int getLevelCount() {
        return Levels.count();
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public Ball getBall() {
        return ball;
    }

    public Paddle getPaddle() {
        return paddle;
    }

    public List<Brick> getBricks() {
        return Collections.unmodifiableList(bricks);
    }
}
