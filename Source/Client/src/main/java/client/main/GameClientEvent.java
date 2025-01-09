package client.main;

import java.util.Arrays;
import java.util.Collection;

public enum GameClientEvent {
    HAS_LOST,
    HAS_WON,
    SHOULD_DRAW_MAP,
    SHOULD_SEND_MOVE,
    SHOULD_SUSPEND,
    SHOULD_UPDATE_STATE,
    STAGE_COMPLETE,
    STAGE_START;

    public static Collection<GameClientEvent> allEvents() {
        return Arrays.stream(values()).toList();
    }
}
