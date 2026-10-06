package com.dagen.arkanoid.game;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

public class LevelsTest {

    @Test
    public void layoutsUseOnlyKnownCellsAndFitTheField() {
        for (int i = 0; i < Levels.count(); i++) {
            for (String row : Levels.layout(i)) {
                assertTrue("level " + i + " row too wide: " + row, row.length() <= Levels.COLUMNS);
                assertTrue("level " + i + " bad row: " + row, row.matches("[.#1-4]*"));
            }
        }
    }

    @Test
    public void everyLevelHasBreakableBricks() {
        for (int i = 0; i < Levels.count(); i++) {
            assertTrue(new GameWorld(Difficulty.NORMAL, i).remainingBricks() > 0);
        }
    }

    @Test
    public void layoutReturnsCopy() {
        assertNotSame(Levels.layout(0), Levels.layout(0));
    }
}
