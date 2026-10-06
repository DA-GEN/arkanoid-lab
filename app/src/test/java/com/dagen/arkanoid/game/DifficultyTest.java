package com.dagen.arkanoid.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DifficultyTest {

    @Test
    public void harderLevelsAreActuallyHarder() {
        Difficulty[] all = Difficulty.values();
        for (int i = 1; i < all.length; i++) {
            Difficulty easier = all[i - 1];
            Difficulty harder = all[i];
            assertTrue(harder.getBallSpeed() > easier.getBallSpeed());
            assertTrue(harder.getPaddleWidthRatio() < easier.getPaddleWidthRatio());
            assertTrue(harder.getLives() <= easier.getLives());
            assertTrue(harder.getScoreMultiplier() > easier.getScoreMultiplier());
        }
    }

    @Test
    public void fromNameParsesKnownValues() {
        assertEquals(Difficulty.HARD, Difficulty.fromName("HARD"));
        assertEquals(Difficulty.EASY, Difficulty.fromName("EASY"));
    }

    @Test
    public void fromNameFallsBackToNormal() {
        assertEquals(Difficulty.NORMAL, Difficulty.fromName(null));
        assertEquals(Difficulty.NORMAL, Difficulty.fromName("impossible"));
    }
}
