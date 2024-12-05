package client.map;

import messagesbase.messagesfromclient.ETerrain;
import client.util.ANSIColor;

public enum TerrainType {
    GRASS(1, 1, 0),
    MOUNTAIN(2, 2, 1),
    WATER(Integer.MAX_VALUE, Integer.MAX_VALUE, 0);

    private final int enterCost;
    private final int leaveCost;
    private final int viewRadius;

    TerrainType(int enterCost, int leaveCost, int viewRadius) {
        this.enterCost = enterCost;
        this.leaveCost = leaveCost;
        this.viewRadius = viewRadius;
    }

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

    public int getEnterCost() {
        return enterCost;
    }

    public int getLeaveCost() {
        return leaveCost;
    }

    public int getViewRadius() {
        return viewRadius;
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
