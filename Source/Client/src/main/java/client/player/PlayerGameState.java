package client.player;

import messagesbase.messagesfromserver.EPlayerGameState;

public enum PlayerGameState {
    LOST,
    MUST_ACT,
    MUST_WAIT,
    WON;

    public static PlayerGameState fromEPlayerGameState(EPlayerGameState playerGameState) {
        return switch (playerGameState) {
            case Lost -> LOST;
            case MustAct -> MUST_ACT;
            case MustWait -> MUST_WAIT;
            case Won -> WON;
        };
    }
}
