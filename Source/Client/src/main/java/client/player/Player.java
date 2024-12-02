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

    private void throwOnInvalidUpdateId(Player newPlayer) {
        String errorMessage = String.format("Player with id %s was updated with new one with id %s",
                                            playerId,
                                            newPlayer.playerId);

        throw new IllegalArgumentException(errorMessage);
    }

    public void update(Player newPlayer) {
        if (!playerId.equals(newPlayer.playerId)) {
            throwOnInvalidUpdateId(newPlayer);
        }

        state = newPlayer.state;
        position = newPlayer.position;
        hasTreasure = newPlayer.hasTreasure;
    }

    public boolean shouldPlayerAct() {
        return state == PlayerGameState.MUST_ACT;
    }
}
