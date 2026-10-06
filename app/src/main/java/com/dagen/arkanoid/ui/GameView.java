package com.dagen.arkanoid.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.view.DisplayCutout;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowInsets;

import com.dagen.arkanoid.R;
import com.dagen.arkanoid.game.Ball;
import com.dagen.arkanoid.game.Brick;
import com.dagen.arkanoid.game.GameState;
import com.dagen.arkanoid.game.GameWorld;
import com.dagen.arkanoid.game.Paddle;

import java.text.NumberFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressLint("ViewConstructor")
public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private static final Logger LOG = Logger.getLogger(GameView.class.getName());

    private static final long FRAME_NANOS = 1_000_000_000L / 60;
    private static final float PAUSE_LEFT = GameWorld.WIDTH - 100f;
    private static final float PAUSE_TOP = 20f;
    private static final float PAUSE_SIZE = 80f;
    private static final float OVERLAY_Y = 1050f;
    private static final int[] BRICK_COLORS = {
            0xFF66BB6A, 0xFFFFCA28, 0xFFFF7043, 0xFFEF5350,
    };
    private static final int UNBREAKABLE_COLOR = 0xFF78909C;

    private final GameWorld world;
    private final SurfaceHolder holder;
    private final NumberFormat numberFormat;

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final int backgroundColor;
    private final int fieldColor;
    private final int accentColor;
    private final int textColor;
    private final int secondaryTextColor;

    private Thread thread;
    private volatile boolean running;
    private volatile boolean newRecord;

    private float scale = 1f;
    private float offsetX;
    private float offsetY;
    private int safeLeft;
    private int safeTop;
    private int safeRight;
    private int safeBottom;

    private boolean touchOnPause;
    private float lastTouchX;
    private float lastTouchY;

    public GameView(Context context, GameWorld world) {
        super(context);
        this.world = world;
        this.holder = getHolder();
        holder.addCallback(this);
        setFocusable(true);

        Resources res = context.getResources();
        numberFormat = NumberFormat.getIntegerInstance(res.getConfiguration().getLocales().get(0));
        backgroundColor = context.getColor(R.color.background);
        fieldColor = context.getColor(R.color.surface);
        accentColor = context.getColor(R.color.accent);
        textColor = context.getColor(R.color.text_primary);
        secondaryTextColor = context.getColor(R.color.text_secondary);

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(4f);
        textPaint.setTextSize(40f);
        textPaint.setColor(textColor);
        titlePaint.setTextSize(72f);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setColor(textColor);
    }

    public void setNewRecord(boolean newRecord) {
        this.newRecord = newRecord;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        running = true;
        thread = new Thread(this, "game-loop");
        thread.start();
        LOG.fine("game loop started");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        updateViewport(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        running = false;
        world.pause();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.log(Level.WARNING, "interrupted while stopping game loop", e);
        }
        LOG.fine("game loop stopped");
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            DisplayCutout cutout = insets.getDisplayCutout();
            if (cutout != null) {
                safeLeft = cutout.getSafeInsetLeft();
                safeTop = cutout.getSafeInsetTop();
                safeRight = cutout.getSafeInsetRight();
                safeBottom = cutout.getSafeInsetBottom();
                updateViewport(getWidth(), getHeight());
            }
        }
        return super.onApplyWindowInsets(insets);
    }

    private void updateViewport(int width, int height) {
        float availableW = width - safeLeft - safeRight;
        float availableH = height - safeTop - safeBottom;
        if (availableW <= 0 || availableH <= 0) {
            return;
        }
        scale = Math.min(availableW / GameWorld.WIDTH, availableH / GameWorld.HEIGHT);
        offsetX = safeLeft + (availableW - GameWorld.WIDTH * scale) / 2f;
        offsetY = safeTop + (availableH - GameWorld.HEIGHT * scale) / 2f;
        LOG.fine(String.format("viewport %dx%d, scale %.3f", width, height, scale));
    }

    @Override
    public void run() {
        long last = System.nanoTime();
        while (running) {
            long frameStart = System.nanoTime();
            world.update((frameStart - last) / 1_000_000_000f);
            last = frameStart;

            Canvas canvas = holder.lockHardwareCanvas();
            if (canvas != null) {
                try {
                    render(canvas);
                } finally {
                    holder.unlockCanvasAndPost(canvas);
                }
            }

            long sleepNanos = FRAME_NANOS - (System.nanoTime() - frameStart);
            if (sleepNanos > 0) {
                try {
                    Thread.sleep(sleepNanos / 1_000_000L, (int) (sleepNanos % 1_000_000L));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = toWorldX(event.getX());
        float y = toWorldY(event.getY());
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touchOnPause = isPauseButton(x, y);
                if (!touchOnPause) {
                    world.movePaddle(x);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!touchOnPause) {
                    world.movePaddle(x);
                }
                return true;
            case MotionEvent.ACTION_UP:
                lastTouchX = x;
                lastTouchY = y;
                performClick();
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        if (touchOnPause && isPauseButton(lastTouchX, lastTouchY)) {
            world.togglePause();
        } else if (world.getState() != GameState.RUNNING) {
            world.tap();
        }
        return true;
    }

    private boolean isPauseButton(float x, float y) {
        float pad = 20f;
        return x >= PAUSE_LEFT - pad && x <= PAUSE_LEFT + PAUSE_SIZE + pad
                && y >= PAUSE_TOP - pad && y <= PAUSE_TOP + PAUSE_SIZE + pad;
    }

    private float toWorldX(float screenX) {
        return (screenX - offsetX) / scale;
    }

    private float toWorldY(float screenY) {
        return (screenY - offsetY) / scale;
    }

    private void render(Canvas canvas) {
        canvas.drawColor(backgroundColor);
        canvas.save();
        canvas.translate(offsetX, offsetY);
        canvas.scale(scale, scale);
        synchronized (world) {
            drawField(canvas);
            drawBricks(canvas);
            drawPaddle(canvas);
            drawBall(canvas);
            drawHud(canvas);
            drawOverlay(canvas);
        }
        canvas.restore();
    }

    private void drawField(Canvas canvas) {
        fillPaint.setColor(fieldColor);
        canvas.drawRect(0f, GameWorld.HUD_HEIGHT, GameWorld.WIDTH, GameWorld.HEIGHT, fillPaint);
        strokePaint.setColor(accentColor);
        canvas.drawLine(0f, GameWorld.HUD_HEIGHT, GameWorld.WIDTH, GameWorld.HUD_HEIGHT, strokePaint);
    }

    private void drawBricks(Canvas canvas) {
        for (Brick brick : world.getBricks()) {
            if (!brick.isAlive()) {
                continue;
            }
            if (brick.isUnbreakable()) {
                fillPaint.setColor(UNBREAKABLE_COLOR);
            } else {
                int index = Math.min(brick.getHitsLeft(), BRICK_COLORS.length) - 1;
                fillPaint.setColor(BRICK_COLORS[index]);
            }
            canvas.drawRoundRect(brick.getLeft(), brick.getTop(), brick.getRight(), brick.getBottom(),
                    8f, 8f, fillPaint);
            if (!brick.isUnbreakable() && brick.getHitsLeft() < brick.getMaxHits()) {
                strokePaint.setColor(Color.argb(120, 0, 0, 0));
                canvas.drawLine(brick.getLeft() + 10f, brick.getTop() + 10f,
                        brick.getRight() - 10f, brick.getBottom() - 10f, strokePaint);
            }
        }
    }

    private void drawPaddle(Canvas canvas) {
        Paddle paddle = world.getPaddle();
        fillPaint.setColor(accentColor);
        canvas.drawRoundRect(paddle.getLeft(), paddle.getTop(), paddle.getRight(), paddle.getBottom(),
                13f, 13f, fillPaint);
    }

    private void drawBall(Canvas canvas) {
        Ball ball = world.getBall();
        fillPaint.setColor(textColor);
        canvas.drawCircle(ball.getX(), ball.getY(), ball.getRadius(), fillPaint);
    }

    private void drawHud(Canvas canvas) {
        float baseline = 75f;
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(getContext().getString(R.string.hud_score, numberFormat.format(world.getScore())),
                24f, baseline, textPaint);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(getContext().getString(R.string.hud_level, world.getLevel(), world.getLevelCount()),
                GameWorld.WIDTH / 2f + 40f, baseline, textPaint);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(getContext().getString(R.string.hud_lives, world.getLives()),
                PAUSE_LEFT - 24f, baseline, textPaint);

        fillPaint.setColor(world.getState() == GameState.PAUSED ? accentColor : secondaryTextColor);
        float barWidth = PAUSE_SIZE / 4f;
        float top = PAUSE_TOP + 15f;
        float bottom = PAUSE_TOP + PAUSE_SIZE - 15f;
        canvas.drawRect(PAUSE_LEFT + barWidth, top, PAUSE_LEFT + 2 * barWidth - 6f, bottom, fillPaint);
        canvas.drawRect(PAUSE_LEFT + 2 * barWidth + 6f, top, PAUSE_LEFT + 3 * barWidth, bottom, fillPaint);
    }

    private void drawOverlay(Canvas canvas) {
        Resources res = getResources();
        GameState state = world.getState();
        String title;
        String subtitle;
        String hint = null;
        switch (state) {
            case READY:
                title = res.getString(R.string.overlay_ready);
                subtitle = res.getQuantityString(R.plurals.lives_left, world.getLives(), world.getLives());
                hint = res.getString(R.string.overlay_hint);
                break;
            case PAUSED:
                title = res.getString(R.string.overlay_paused);
                subtitle = res.getString(R.string.overlay_resume);
                break;
            case LEVEL_CLEARED:
                title = res.getString(R.string.overlay_level_cleared, world.getLevel());
                subtitle = res.getString(R.string.overlay_next_level);
                break;
            case GAME_OVER:
            case VICTORY:
                title = res.getString(state == GameState.VICTORY ? R.string.overlay_victory : R.string.overlay_game_over);
                subtitle = res.getString(R.string.overlay_final_score, numberFormat.format(world.getScore()));
                hint = res.getString(R.string.overlay_play_again);
                break;
            default:
                return;
        }

        if (state != GameState.READY) {
            fillPaint.setColor(Color.argb(170, 0, 0, 0));
            canvas.drawRect(0f, GameWorld.HUD_HEIGHT, GameWorld.WIDTH, GameWorld.HEIGHT, fillPaint);
        }
        canvas.drawText(title, GameWorld.WIDTH / 2f, OVERLAY_Y, titlePaint);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(textColor);
        canvas.drawText(subtitle, GameWorld.WIDTH / 2f, OVERLAY_Y + 80f, textPaint);
        textPaint.setColor(secondaryTextColor);
        if (hint != null) {
            canvas.drawText(hint, GameWorld.WIDTH / 2f, OVERLAY_Y + 140f, textPaint);
        }
        if (state.isFinished() && newRecord) {
            textPaint.setColor(accentColor);
            canvas.drawText(res.getString(R.string.overlay_new_record), GameWorld.WIDTH / 2f, OVERLAY_Y + 200f, textPaint);
        }
    }
}
