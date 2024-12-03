package client.map;

import messagesbase.messagesfromclient.ETerrain;
import client.util.ANSIColor;

public enum TerrainType {
    GRASS,
    MOUNTAIN,
    WATER;

    public static TerrainType fromETerrain(ETerrain terrain) {
        return switch (terrain) {
            case Grass -> GRASS;
            case Mountain -> MOUNTAIN;
            case Water -> WATER;
        };
    }

    public ETerrain intoETerrain() {
        return switch (this) {
            case GRASS -> ETerrain.Grass;
            case MOUNTAIN -> ETerrain.Mountain;
            case WATER -> ETerrain.Water;
        };
    }

    @Override
    public String toString() {
        return switch (this) {
            case GRASS -> ANSIColor.format("G", ANSIColor.BRIGHT_BLACK, ANSIColor.GREEN);
            case MOUNTAIN -> ANSIColor.format("M", ANSIColor.WHITE, ANSIColor.BRIGHT_BLACK);
            case WATER -> ANSIColor.format("W", ANSIColor.BRIGHT_BLACK, ANSIColor.BLUE);
        };
    }
}
