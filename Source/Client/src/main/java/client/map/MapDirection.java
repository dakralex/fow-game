package client.map;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Predicate;

import messagesbase.messagesfromclient.EMove;

public enum MapDirection {
    EAST(1, 0),
    NORTH(0, -1),
    SOUTH(0, 1),
    WEST(-1, 0);

    private final int dx;
    private final int dy;

    MapDirection(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public static MapDirection randomDirection(Random random) {
        int randomValue = random.nextInt(0, values().length - 1);

        return values()[randomValue];
    }

    private static Predicate<MapDirection> equalsTo(int dx, int dy) {
        return direction -> direction.dx == dx && direction.dy == dy;
    }

    public static MapDirection fromDifferentials(int dx, int dy) {
        return Arrays.stream(values())
                .filter(equalsTo(dx, dy))
                .findFirst()
                .orElseThrow(() -> {
                    String errorMessage = String.format("No direction found for (%d, %d)", dx, dy);

                    return new IllegalArgumentException(errorMessage);
                });
    }

    public EMove intoEMove() {
        return switch (this) {
            case EAST -> EMove.Right;
            case NORTH -> EMove.Up;
            case SOUTH -> EMove.Down;
            case WEST -> EMove.Left;
        };
    }

    /**
     * Returns the opposite direction.
     *
     * @return opposite direction
     */
    public MapDirection getOpposite() {
        return switch (this) {
            case EAST -> WEST;
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case WEST -> EAST;
        };
    }

    /**
     * Returns the direction perpendicular to the right.
     * <p>
     * For example, the perpendicular direction of {@link #NORTH} to the right is {@link #EAST}.
     * <p>
     * NORTH
     * ^
     * |
     * |
     * -----> EAST
     *
     * @return direction perpendicular to the right
     */
    public MapDirection getPerpendicular() {
        return switch (this) {
            case EAST -> SOUTH;
            case NORTH -> EAST;
            case SOUTH -> WEST;
            case WEST -> NORTH;
        };
    }

    public int getOffsetX() {
        return dx;
    }

    public int getOffsetY() {
        return dy;
    }
}
