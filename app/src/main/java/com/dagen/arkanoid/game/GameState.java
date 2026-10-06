package com.dagen.arkanoid.game;

public enum GameState {
    READY,
    RUNNING,
    PAUSED,
    LEVEL_CLEARED,
    GAME_OVER,
    VICTORY;

    public boolean isFinished() {
        return this == GAME_OVER || this == VICTORY;
    }
}
