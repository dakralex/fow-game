package client.map;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public record PositionArea(int x, int y, int width, int height) {

    public PositionArea(Position upperLeft, Position lowerRight) {
        this(upperLeft.x(),
             upperLeft.y(),
             lowerRight.x() + 1 - upperLeft.x(),
             lowerRight.y() + 1 - upperLeft.y());
    }

    public Stream<Position> intoPositionStream() {
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

    public boolean isOnEastBorder(Position position) {
        return position.x() == x + width - 1;
    }

    public boolean isOnNorthBorder(Position position) {
        return position.y() == y;
    }

    public boolean isOnSouthBorder(Position position) {
        return position.y() == y + height - 1;
    }

    public boolean isOnWestBorder(Position position) {
        return position.x() == x;
    }

    public boolean isLandscape() {
        return width > height;
    }

    public Position middlePoint() {
        return new Position((width - x) / 2, (height - y) / 2);
    }
}
