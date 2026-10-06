package com.dagen.arkanoid.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.dagen.arkanoid.game.Difficulty;
import com.dagen.arkanoid.i18n.AppLanguage;

import java.util.Locale;
import java.util.logging.Logger;

public class GamePrefs {
    static final String FILE_NAME = "arkanoid_prefs";

    private static final String KEY_DIFFICULTY = "difficulty";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_BEST_PREFIX = "best_score_";
    private static final Logger LOG = Logger.getLogger(GamePrefs.class.getName());

    private final SharedPreferences prefs;

    public GamePrefs(Context context) {
        prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public Difficulty getDifficulty() {
        return Difficulty.fromName(prefs.getString(KEY_DIFFICULTY, null));
    }

    public void setDifficulty(Difficulty difficulty) {
        prefs.edit().putString(KEY_DIFFICULTY, difficulty.name()).apply();
    }

    public AppLanguage getLanguage() {
        return AppLanguage.fromTag(prefs.getString(KEY_LANGUAGE, null));
    }

    public void setLanguage(AppLanguage language) {
        prefs.edit().putString(KEY_LANGUAGE, language.getTag()).apply();
    }

    public int getBestScore(Difficulty difficulty) {
        return prefs.getInt(KEY_BEST_PREFIX + difficulty.name(), 0);
    }

    /** Stores the score if it beats the current best one. */
    public boolean submitScore(Difficulty difficulty, int score) {
        int best = getBestScore(difficulty);
        if (score <= best) {
            return false;
        }
        prefs.edit().putInt(KEY_BEST_PREFIX + difficulty.name(), score).apply();
        LOG.info(String.format(Locale.ROOT, "new best score on %s: %d (was %d)", difficulty, score, best));
        return true;
    }
}
