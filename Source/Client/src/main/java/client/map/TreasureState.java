package client.map;

import messagesbase.messagesfromserver.ETreasureState;

public enum TreasureState {
    UNKNOWN_OR_NONE_PRESENT,
    PLAYER_TREASURE_PRESENT;

    public static TreasureState fromETreasureState(ETreasureState treasureState) {
        return switch (treasureState) {
            case NoOrUnknownTreasureState -> UNKNOWN_OR_NONE_PRESENT;
            case MyTreasureIsPresent -> PLAYER_TREASURE_PRESENT;
        };
    }

    @Override
    public String toString() {
        return switch (this) {
            case UNKNOWN_OR_NONE_PRESENT -> "?";
            case PLAYER_TREASURE_PRESENT -> "P";
        };
    }
}
