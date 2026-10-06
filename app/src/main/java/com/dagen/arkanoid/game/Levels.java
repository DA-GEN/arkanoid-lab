package com.dagen.arkanoid.game;

/**
 * Brick layouts. A digit is the number of hits a brick takes,
 * '#' is an unbreakable brick and '.' is an empty cell.
 */
public final class Levels {
    public static final int COLUMNS = 10;
    public static final int MAX_HITS = 4;

    private static final String[][] LAYOUTS = {
            {
                    "1111111111",
                    "1111111111",
                    "1111111111",
                    "1111111111",
                    "1111111111",
            },
            {
                    "....22....",
                    "...2112...",
                    "..211112..",
                    ".21111112.",
                    "2111111112",
                    "1111111111",
            },
            {
                    "3333333333",
                    "2.2.22.2.2",
                    "1111111111",
                    "#..1111..#",
                    "2222222222",
                    "1..1..1..1",
                    "##..##..##",
            },
    };

    private Levels() {
    }

    public static int count() {
        return LAYOUTS.length;
    }

    public static String[] layout(int index) {
        return LAYOUTS[index].clone();
    }
}
