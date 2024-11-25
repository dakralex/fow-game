package client.main;

import java.util.Optional;
import java.util.Set;

import client.map.GameMap;
import client.map.Position;
import client.player.Player;
import messagesbase.messagesfromserver.GameState;
import messagesbase.messagesfromserver.PlayerState;

public class GameClientState {

    private final String gameId;
    private final String stateId;
    private final GameMap map;
    private final Player player;
    private final Optional<Player> opponent;

    public GameClientState(String gameId, String stateId, GameMap map,
                           Player player, Optional<Player> opponent) {
        this.gameId = gameId;
        this.stateId = stateId;
        this.map = map;
        this.player = player;
        this.opponent = opponent;
    }

    public static PlayerState pickOwnPlayer(Set<PlayerState> players, String playerId) {
        return players.stream()
                .filter(playerState -> playerState.getUniquePlayerID().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new GameClientException(
                        "Failed to get player information: Player not found."));
    }

    public static Optional<PlayerState> pickOtherPlayer(Set<PlayerState> players,
                                                        PlayerState player) {
        return players.stream()
                .filter(playerState -> !playerState.equals(player))
                .findFirst();
    }

    public static GameClientState fromGameState(GameState gameState, String gameId,
                                                String playerId) {
        String stateId = gameState.getGameStateId();
        GameMap map = GameMap.fromFullMap(gameState.getMap());

        Position playerPosition = GameMap.getPlayerPosition(gameState.getMap());
        PlayerState playerState = pickOwnPlayer(gameState.getPlayers(), playerId);
        Player player = Player.fromPlayerState(playerState, playerPosition);

        Position opponentPosition = GameMap.getOpponentPosition(gameState.getMap());
        Optional<PlayerState> opponentState = pickOtherPlayer(gameState.getPlayers(), playerState);
        Optional<Player> opponent = opponentState
                .map(state -> Player.fromPlayerState(state, opponentPosition));

        return new GameClientState(gameId, stateId, map, player, opponent);
    }

    public boolean hasBothPlayers() {
        return opponent.isPresent();
    }

    public boolean shouldClientAct() {
        return player.shouldPlayerAct();
    }
}
