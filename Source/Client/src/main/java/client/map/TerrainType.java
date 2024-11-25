package client.map;

import messagesbase.messagesfromclient.ETerrain;

public enum TerrainType {
    GRASS,
    MOUNTAIN,
    WATER;

    public ETerrain intoETerrain() {
        return switch (this) {
            case GRASS -> ETerrain.Grass;
            case MOUNTAIN -> ETerrain.Mountain;
            case WATER -> ETerrain.Water;
        };
    }
}
