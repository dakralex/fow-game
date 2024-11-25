package client.map;

import messagesbase.messagesfromserver.FullMapNode;

public record Position(int x, int y) {

    public static final Position originPosition = new Position(0, 0);

    public static Position fromFullMapNode(FullMapNode fullMapNode) {
        return new Position(fullMapNode.getX(), fullMapNode.getY());
    }
}
