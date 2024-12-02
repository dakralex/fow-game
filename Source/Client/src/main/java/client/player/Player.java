package client.player;

import client.map.Position;
import messagesbase.messagesfromserver.PlayerState;

public class Player {

    private final String playerId;
    private final PlayerDetails details;
    private PlayerGameState state;
    private Position position;
    private boolean hasTreasure;

    public Player(String playerId, PlayerDetails details) {
        this.playerId = playerId;
        this.details = details;
        this.position = new Position(0, 0);
        this.hasTreasure = false;
    }

    public Player(String playerId, PlayerDetails details, PlayerGameState state,
                  Position position, boolean hasTreasure) {
        this.playerId = playerId;
        this.details = details;
        this.state = state;
        this.position = position;
        this.hasTreasure = hasTreasure;
    }

    public static Player fromPlayerState(PlayerState playerState, Position position) {
        String playerId = playerState.getUniquePlayerID();
        PlayerDetails details = PlayerDetails.fromPlayerState(playerState);
        PlayerGameState state = PlayerGameState.fromEPlayerGameState(playerState.getState());
        boolean hasTreasure = playerState.hasCollectedTreasure();

        return new Player(playerId, details, state, position, hasTreasure);
    }

    public boolean shouldPlayerAct() {
        return state == PlayerGameState.MUST_ACT;
    }
}
