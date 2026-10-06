package com.dagen.arkanoid.game;

public final class Paddle {
    private final float width;
    private final float height;
    private final float top;
    private final float minX;
    private final float maxX;
    private float centerX;

    public Paddle(float width, float height, float top, float minX, float maxX) {
        this.width = width;
        this.height = height;
        this.top = top;
        this.minX = minX;
        this.maxX = maxX;
        this.centerX = (minX + maxX) / 2f;
    }

    public void moveTo(float x) {
        float half = width / 2f;
        centerX = Math.max(minX + half, Math.min(maxX - half, x));
    }

    public float getCenterX() {
        return centerX;
    }

    public float getLeft() {
        return centerX - width / 2f;
    }

    public float getRight() {
        return centerX + width / 2f;
    }

    public float getTop() {
        return top;
    }

    public float getBottom() {
        return top + height;
    }

    public float getWidth() {
        return width;
    }
}
