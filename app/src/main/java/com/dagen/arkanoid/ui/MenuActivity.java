package com.dagen.arkanoid.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.dagen.arkanoid.R;
import com.dagen.arkanoid.data.GamePrefs;
import com.dagen.arkanoid.game.Difficulty;

import java.text.NumberFormat;
import java.util.logging.Logger;

public class MenuActivity extends Activity {
    private static final Logger LOG = Logger.getLogger(MenuActivity.class.getName());

    private static final int[] DIFFICULTY_BUTTONS = {
            R.id.difficulty_easy, R.id.difficulty_normal, R.id.difficulty_hard,
    };
    private static final int[] DIFFICULTY_DESCRIPTIONS = {
            R.string.difficulty_easy_description,
            R.string.difficulty_normal_description,
            R.string.difficulty_hard_description,
    };

    private GamePrefs prefs;
    private RadioGroup difficultyGroup;
    private TextView descriptionView;
    private TextView bestScoreView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        prefs = new GamePrefs(this);

        difficultyGroup = findViewById(R.id.difficulty_group);
        descriptionView = findViewById(R.id.difficulty_description);
        bestScoreView = findViewById(R.id.best_score);

        difficultyGroup.check(DIFFICULTY_BUTTONS[prefs.getDifficulty().ordinal()]);
        difficultyGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Difficulty difficulty = selectedDifficulty();
            prefs.setDifficulty(difficulty);
            LOG.fine("difficulty selected: " + difficulty);
            updateDifficultyInfo();
        });
        findViewById(R.id.play_button).setOnClickListener(v -> startGame());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDifficultyInfo();
    }

    private Difficulty selectedDifficulty() {
        int checked = difficultyGroup.getCheckedRadioButtonId();
        for (int i = 0; i < DIFFICULTY_BUTTONS.length; i++) {
            if (DIFFICULTY_BUTTONS[i] == checked) {
                return Difficulty.values()[i];
            }
        }
        return Difficulty.NORMAL;
    }

    private void updateDifficultyInfo() {
        Difficulty difficulty = selectedDifficulty();
        Resources res = getResources();
        NumberFormat format = NumberFormat.getIntegerInstance(res.getConfiguration().getLocales().get(0));

        String lives = res.getQuantityString(R.plurals.lives, difficulty.getLives(), difficulty.getLives());
        descriptionView.setText(getString(R.string.difficulty_details,
                getString(DIFFICULTY_DESCRIPTIONS[difficulty.ordinal()]), lives, difficulty.getScoreMultiplier()));
        bestScoreView.setText(getString(R.string.menu_best_score, format.format(prefs.getBestScore(difficulty))));
    }

    private void startGame() {
        Difficulty difficulty = selectedDifficulty();
        LOG.info("play pressed, difficulty: " + difficulty);
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_DIFFICULTY, difficulty.name());
        startActivity(intent);
    }
}
