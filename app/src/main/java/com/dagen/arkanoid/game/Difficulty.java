package com.dagen.arkanoid.game;

public enum Difficulty {
    EASY(620f, 0.26f, 5, 0, 1),
    NORMAL(820f, 0.20f, 3, 0, 2),
    HARD(1020f, 0.15f, 2, 1, 3);

    private final float ballSpeed;
    private final float paddleWidthRatio;
    private final int lives;
    private final int extraBrickHits;
    private final int scoreMultiplier;

    Difficulty(float ballSpeed, float paddleWidthRatio, int lives, int extraBrickHits, int scoreMultiplier) {
        this.ballSpeed = ballSpeed;
        this.paddleWidthRatio = paddleWidthRatio;
        this.lives = lives;
        this.extraBrickHits = extraBrickHits;
        this.scoreMultiplier = scoreMultiplier;
    }

    public float getBallSpeed() {
        return ballSpeed;
    }

    public float getPaddleWidthRatio() {
        return paddleWidthRatio;
    }

    public int getLives() {
        return lives;
    }

    public int getExtraBrickHits() {
        return extraBrickHits;
    }

    public int getScoreMultiplier() {
        return scoreMultiplier;
    }

    public static Difficulty fromName(String name) {
        if (name != null) {
            for (Difficulty d : values()) {
                if (d.name().equals(name)) {
                    return d;
                }
            }
        }
        return NORMAL;
    }
}
