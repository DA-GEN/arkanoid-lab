package com.dagen.arkanoid.ui;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import com.dagen.arkanoid.data.GamePrefs;
import com.dagen.arkanoid.game.Difficulty;
import com.dagen.arkanoid.game.GameWorld;

import java.util.logging.Logger;

public class GameActivity extends BaseActivity {
    public static final String EXTRA_DIFFICULTY = "com.dagen.arkanoid.DIFFICULTY";

    private static final Logger LOG = Logger.getLogger(GameActivity.class.getName());

    private GameWorld world;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Difficulty difficulty = Difficulty.fromName(getIntent().getStringExtra(EXTRA_DIFFICULTY));
        LOG.info("starting game, difficulty: " + difficulty);

        world = new GameWorld(difficulty);
        GameView gameView = new GameView(this, world);
        GamePrefs prefs = new GamePrefs(this);
        world.setListener(new GameWorld.Listener() {
            @Override
            public void onGameFinished(int score, boolean victory) {
                gameView.setNewRecord(prefs.submitScore(difficulty, score));
            }
        });
        setContentView(gameView);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideSystemBars();
    }

    @Override
    protected void onPause() {
        super.onPause();
        world.pause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemBars();
        }
    }

    @SuppressWarnings("deprecation")
    private void hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.systemBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }
}
