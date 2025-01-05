package client.map;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record PositionArea(int x, int y, int width, int height) {

    public PositionArea(Position upperLeft, Position lowerRight) {
        this(upperLeft.x(),
             upperLeft.y(),
             lowerRight.x() + 1 - upperLeft.x(),
             lowerRight.y() + 1 - upperLeft.y());
    }

    public Stream<Position> intoStream() {
        return IntStream.range(0, width * height)
                .mapToObj(index -> {
                    int posX = x + index % width;
                    int posY = y + Math.floorDiv(index, width);

                    return new Position(posX, posY);
                });
    }

    private boolean isInside(Position position) {
        int posX = position.x();
        int posY = position.y();

        boolean withinXBoundary = posX >= x && posX < (x + width);
        boolean withinYBoundary = posY >= y && posY < (y + height);

        return withinXBoundary && withinYBoundary;
    }

    public boolean isOutside(Position position) {
        return !isInside(position);
    }

    private boolean isOnEastBorder(Position position) {
        return position.x() == x + width - 1;
    }

    private boolean isOnNorthBorder(Position position) {
        return position.y() == y;
    }

    private boolean isOnSouthBorder(Position position) {
        return position.y() == y + height - 1;
    }

    private boolean isOnWestBorder(Position position) {
        return position.x() == x;
    }

    public Predicate<Position> getBorderPredicate(MapDirection direction) {
        return switch (direction) {
            case EAST -> this::isOnEastBorder;
            case NORTH -> this::isOnNorthBorder;
            case SOUTH -> this::isOnSouthBorder;
            case WEST -> this::isOnWestBorder;
        };
    }

    public Position upperLeft() {
        return new Position(x, y);
    }

    public Position upperRight() {
        return new Position(x + width - 1, y);
    }

    public Position lowerLeft() {
        return new Position(x, y + height - 1);
    }

    public Position lowerRight() {
        return new Position(x + width - 1, y + height - 1);
    }

    private Collection<Position> corners() {
        return List.of(upperLeft(), upperRight(), lowerLeft(), lowerRight());
    }

    public boolean isCorner(Position position) {
        return corners().stream().anyMatch(position::equals);
    }

    public Position middlePoint() {
        return new Position(x + width / 2, y + height / 2);
    }

    public boolean isMiddle(Position position) {
        Position middlePoint = middlePoint();

        return isInside(position)
                && (position.x() == middlePoint.x() || position.y() == middlePoint.y());
    }

    public boolean isLandscape() {
        return width > height;
    }
}
