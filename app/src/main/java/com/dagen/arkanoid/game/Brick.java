package com.dagen.arkanoid.game;

public final class Brick {
    private final float left;
    private final float top;
    private final float right;
    private final float bottom;
    private final int maxHits;
    private final boolean unbreakable;
    private int hitsLeft;

    private Brick(float left, float top, float right, float bottom, int hits, boolean unbreakable) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.maxHits = hits;
        this.hitsLeft = hits;
        this.unbreakable = unbreakable;
    }

    public static Brick breakable(float left, float top, float right, float bottom, int hits) {
        if (hits <= 0) {
            throw new IllegalArgumentException("hits must be positive: " + hits);
        }
        return new Brick(left, top, right, bottom, hits, false);
    }

    public static Brick unbreakable(float left, float top, float right, float bottom) {
        return new Brick(left, top, right, bottom, 1, true);
    }

    /** Returns true when this hit destroyed the brick. */
    public boolean hit() {
        if (unbreakable || hitsLeft == 0) {
            return false;
        }
        hitsLeft--;
        return hitsLeft == 0;
    }

    public boolean isAlive() {
        return unbreakable || hitsLeft > 0;
    }

    public boolean isUnbreakable() {
        return unbreakable;
    }

    public int getHitsLeft() {
        return hitsLeft;
    }

    public int getMaxHits() {
        return maxHits;
    }

    public float getLeft() {
        return left;
    }

    public float getTop() {
        return top;
    }

    public float getRight() {
        return right;
    }

    public float getBottom() {
        return bottom;
    }
}
