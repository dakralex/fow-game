package client.map;

import java.util.Random;

public enum MapDirection {
    EAST(1, 0),
    NORTH(0, -1),
    SOUTH(0, 1),
    WEST(-1, 0);

    private final int x;
    private final int y;

    MapDirection(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static MapDirection randomDirection(Random random) {
        int randomValue = random.nextInt(0, values().length - 1);

        return values()[randomValue];
    }

    public MapDirection getOpposite() {
        return switch (this) {
            case EAST -> WEST;
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case WEST -> EAST;
        };
    }

    public MapDirection getPerpendicular() {
        return switch (this) {
            case EAST -> SOUTH;
            case NORTH -> EAST;
            case SOUTH -> WEST;
            case WEST -> NORTH;
        };
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
