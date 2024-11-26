package client.map;

import java.util.Objects;

import messagesbase.messagesfromserver.FullMapNode;

public record Position(int x, int y) implements Comparable<Position> {

    public static final Position originPosition = new Position(0, 0);

    public static Position fromFullMapNode(FullMapNode fullMapNode) {
        return new Position(fullMapNode.getX(), fullMapNode.getY());
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
}
