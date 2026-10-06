package com.dagen.arkanoid.game;

public final class Ball {
    private final float radius;
    private float x;
    private float y;
    private float vx;
    private float vy;

    public Ball(float radius) {
        this.radius = radius;
    }

    public void move(float dt) {
        x += vx * dt;
        y += vy * dt;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setVelocity(float vx, float vy) {
        this.vx = vx;
        this.vy = vy;
    }

    public void setSpeed(float speed) {
        float current = getSpeed();
        if (current > 0f) {
            float k = speed / current;
            vx *= k;
            vy *= k;
        }
    }

    public float getSpeed() {
        return (float) Math.hypot(vx, vy);
    }

    public float getRadius() {
        return radius;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getVx() {
        return vx;
    }

    public float getVy() {
        return vy;
    }

    void setX(float x) {
        this.x = x;
    }

    void setY(float y) {
        this.y = y;
    }

    void setVx(float vx) {
        this.vx = vx;
    }

    void setVy(float vy) {
        this.vy = vy;
    }
}
