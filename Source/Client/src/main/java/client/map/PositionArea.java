package client.map;

public record PositionArea(int x, int y, int width, int height) {
    public boolean isInside(Position position) {
        int posX = position.x();
        int posY = position.y();

        return posX >= x && posX < (x + width) && posY >= y && posY < (y + height);
    }

    public boolean isOnBorder(Position position) {
        int posX = position.x();
        int posY = position.y();

        return posX == x || posX == (x + width - 1) || posY == y || posY == (y + height - 1);
    }

    public boolean isOnBorder(GameMapNode mapNode) {
        return isOnBorder(mapNode.getPosition());
    }
}
