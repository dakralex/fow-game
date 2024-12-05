package client.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;

import client.map.GameMap;
import client.map.Position;
import client.player.Player;
import messagesbase.messagesfromserver.EPlayerPositionState;
import messagesbase.messagesfromserver.FullMap;
import messagesbase.messagesfromserver.FullMapNode;
import messagesbase.messagesfromserver.GameState;
import messagesbase.messagesfromserver.PlayerState;

public class GameClientState {

    private static final Logger logger = LoggerFactory.getLogger(GameClientState.class);

    private final String gameId;
    private final String stateId;
    private final GameMap map;
    private final Player player;
    private Optional<Player> opponent;

    public GameClientState(String gameId, String stateId, GameMap map,
                           Player player, Optional<Player> opponent) {
        this.gameId = gameId;
        this.stateId = stateId;
        this.map = map;
        this.player = player;
        this.opponent = opponent;
    }

    private static PlayerState pickOwnPlayer(Set<PlayerState> players, String playerId) {
        return players.stream()
                .filter(playerState -> playerState.getUniquePlayerID().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new GameClientException(
                        "Failed to get player information: Player not found."));
    }

    private static Optional<PlayerState> pickOtherPlayer(Set<PlayerState> players,
                                                        PlayerState player) {
        return players.stream()
                .filter(playerState -> !playerState.equals(player))
                .findFirst();
    }

    private static boolean isPlayerOnFullMapNode(FullMapNode fullMapNode) {
        return fullMapNode.getPlayerPositionState() == EPlayerPositionState.BothPlayerPosition ||
                fullMapNode.getPlayerPositionState() == EPlayerPositionState.MyPlayerPosition;
    }

    private static Position getPlayerPosition(FullMap fullMap) {
        return fullMap.getMapNodes().stream()
                .filter(GameClientState::isPlayerOnFullMapNode)
                .findFirst()
                .map(Position::fromFullMapNode)
                .orElse(Position.originPosition);
    }

    private static boolean isOpponentOnFullMapNode(FullMapNode fullMapNode) {
        return fullMapNode.getPlayerPositionState() == EPlayerPositionState.BothPlayerPosition ||
                fullMapNode.getPlayerPositionState() == EPlayerPositionState.EnemyPlayerPosition;
    }

    private static Position getOpponentPosition(FullMap fullMap) {
        return fullMap.getMapNodes().stream()
                .filter(GameClientState::isOpponentOnFullMapNode)
                .findFirst()
                .map(Position::fromFullMapNode)
                .orElse(Position.originPosition);
    }

    public static GameClientState fromGameState(GameState gameState, String gameId,
                                                String playerId) {
        String stateId = gameState.getGameStateId();
        GameMap map = GameMap.fromFullMap(gameState.getMap());

        Position playerPosition = getPlayerPosition(gameState.getMap());
        PlayerState playerState = pickOwnPlayer(gameState.getPlayers(), playerId);
        Player player = Player.fromPlayerState(playerState, playerPosition);

        Position opponentPosition = getOpponentPosition(gameState.getMap());
        Optional<PlayerState> opponentState = pickOtherPlayer(gameState.getPlayers(), playerState);
        Optional<Player> opponent = opponentState
                .map(state -> Player.fromPlayerState(state, opponentPosition));

        return new GameClientState(gameId, stateId, map, player, opponent);
    }

    private void updateOpponent(Player newOpponent) {
        if (opponent.isEmpty()) {
            opponent = Optional.of(newOpponent);

            logger.info("Player {} joined the game", newOpponent);
        } else {
            opponent.get().update(newOpponent);
        }
    }

    public void update(GameClientState newState) {
        // Return early, if the new state has not nothing changed
        if (stateId.equals(newState.stateId)) {
            return;
        }

        player.update(newState.player);
        newState.opponent.ifPresent(this::updateOpponent);
        map.update(newState.map, player.getPosition());
    }

    public boolean hasBothPlayers() {
        return opponent.isPresent();
    }

    public boolean shouldClientAct() {
        return player.shouldPlayerAct();
    }
}
