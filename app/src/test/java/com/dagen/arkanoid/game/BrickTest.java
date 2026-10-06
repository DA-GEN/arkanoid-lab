package com.dagen.arkanoid.game;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BrickTest {

    @Test
    public void breakableBrickIsDestroyedAfterAllHits() {
        Brick brick = Brick.breakable(0, 0, 10, 10, 2);
        assertFalse(brick.hit());
        assertTrue(brick.isAlive());
        assertTrue(brick.hit());
        assertFalse(brick.isAlive());
        assertFalse(brick.hit());
    }

    @Test
    public void unbreakableBrickSurvivesHits() {
        Brick brick = Brick.unbreakable(0, 0, 10, 10);
        for (int i = 0; i < 10; i++) {
            assertFalse(brick.hit());
        }
        assertTrue(brick.isAlive());
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroHitsAreRejected() {
        Brick.breakable(0, 0, 10, 10, 0);
    }
}
