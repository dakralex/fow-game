package client.map;

public record PositionArea(int x, int y, int width, int height) {
    public boolean isInside(Position position) {
        int posX = position.x();
        int posY = position.y();

        return posX >= x && posX < (x + width) && posY >= y && posY < (y + height);
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
}
