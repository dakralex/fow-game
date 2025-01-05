package client.map;

import java.util.Objects;

import messagesbase.messagesfromserver.FullMapNode;

public record Position(int x, int y) implements Comparable<Position> {

    public static final Position originPosition = new Position(0, 0);

    public static Position fromFullMapNode(FullMapNode fullMapNode) {
        return new Position(fullMapNode.getX(), fullMapNode.getY());
    }

    public Position stepInDirection(MapDirection direction) {
        return new Position(this.x + direction.getOffsetX(), this.y + direction.getOffsetY());
    }

    public MapDirection getDirection(Position other) {
        return MapDirection.fromDifferentials(other.x - x, other.y - y);
    }

    public MapDirection getHorizontalDirection(Position other) {
        return MapDirection.fromDifferentials(other.x - x, 0);
    }

    public MapDirection getVerticalDirection(Position other) {
        return MapDirection.fromDifferentials(0, other.y - y);
    }

    public int chebyshevDistanceTo(Position other) {
        return Math.max(Math.abs(x - other.x), Math.abs(y - other.y));
    }

    public int taxicabDistanceTo(Position other) {
        return Math.abs(x - other.x) + Math.abs(y - other.y);
    }

    @Override
    public int compareTo(Position other) {
        if (this.y == other.y) {
            return this.x - other.x;
        }

        return this.y - other.y;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Position(int x, int y))) return false;
        return this.x == x && this.y == y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", x, y);
    }
}
