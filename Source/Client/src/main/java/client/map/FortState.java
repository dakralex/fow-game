package client.map;

import messagesbase.messagesfromserver.EFortState;

public enum FortState {
    UNKNOWN,
    NO_FORT_PRESENT,
    PLAYER_FORT_PRESENT,
    ENEMY_FORT_PRESENT;

    public static FortState fromEFortState(EFortState fortState) {
        return switch(fortState) {
            case NoOrUnknownFortState -> NO_FORT_PRESENT;
            case MyFortPresent -> PLAYER_FORT_PRESENT;
            case EnemyFortPresent -> ENEMY_FORT_PRESENT;
        };
    }
}
