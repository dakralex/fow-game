package client.map;

import messagesbase.messagesfromserver.ETreasureState;

public enum TreasureState {
    UNKNOWN,
    NO_TREASURE_PRESENT,
    PLAYER_TREASURE_PRESENT;

    public static TreasureState fromETreasureState(ETreasureState treasureState) {
        return switch (treasureState) {
            case NoOrUnknownTreasureState -> NO_TREASURE_PRESENT;
            case MyTreasureIsPresent -> PLAYER_TREASURE_PRESENT;
        };
    }

    @Override
    public String toString() {
        return switch (this) {
            case UNKNOWN -> "?";
            case NO_TREASURE_PRESENT -> "_";
            case PLAYER_TREASURE_PRESENT -> "P";
        };
    }
}
