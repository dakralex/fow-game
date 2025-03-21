package client.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import client.map.GameMap;
import client.map.GameMapNode;
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
    private Optional<Player> enemy;

    private GameClientState(String gameId, String stateId, GameMap map,
                            Player player, Optional<Player> enemy) {
        this.gameId = gameId;
        this.stateId = stateId;
        this.map = map;
        this.player = player;
        this.enemy = enemy;
    }

    public GameClientState(String gameId, String stateId, GameMap map,
                           Player player, Player enemy) {
        this(gameId, stateId, map, player, Optional.of(enemy));
    }

    private static PlayerState pickOwnPlayer(Collection<PlayerState> players, String playerId) {
        return players.stream()
                .filter(playerState -> playerState.getUniquePlayerID().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new GameClientException(
                        "Failed to get player information: Player not found."));
    }

    private static Optional<PlayerState> pickOtherPlayer(Collection<PlayerState> players,
                                                         PlayerState player) {
        return players.stream()
                .filter(playerState -> !playerState.equals(player))
                .findFirst();
    }

    private static Position getFullMapPosition(FullMap fullMap, Predicate<FullMapNode> predicate) {
        return fullMap.getMapNodes().stream()
                .filter(predicate)
                .findFirst()
                .map(Position::fromFullMapNode)
                .orElse(Position.originPosition);
    }

    private static boolean isPlayerOnFullMapNode(FullMapNode fullMapNode) {
        return fullMapNode.getPlayerPositionState() == EPlayerPositionState.BothPlayerPosition ||
                fullMapNode.getPlayerPositionState() == EPlayerPositionState.MyPlayerPosition;
    }

    private static Position getPlayerPosition(FullMap fullMap) {
        return getFullMapPosition(fullMap, GameClientState::isPlayerOnFullMapNode);
    }

    private static boolean isEnemyOnFullMapNode(FullMapNode fullMapNode) {
        return fullMapNode.getPlayerPositionState() == EPlayerPositionState.BothPlayerPosition ||
                fullMapNode.getPlayerPositionState() == EPlayerPositionState.EnemyPlayerPosition;
    }

    private static Position getEnemyPosition(FullMap fullMap) {
        return getFullMapPosition(fullMap, GameClientState::isEnemyOnFullMapNode);
    }

    public static GameClientState fromGameState(GameState gameState, String gameId,
                                                String playerId) {
        String stateId = gameState.getGameStateId();
        GameMap map = GameMap.fromFullMap(gameState.getMap());

        Position playerPosition = getPlayerPosition(gameState.getMap());
        PlayerState playerState = pickOwnPlayer(gameState.getPlayers(), playerId);
        Player player = Player.fromPlayerState(playerState, playerPosition);

        Position enemyPosition = getEnemyPosition(gameState.getMap());
        Optional<PlayerState> enemyState = pickOtherPlayer(gameState.getPlayers(), playerState);
        Optional<Player> enemy = enemyState
                .map(state -> Player.fromPlayerState(state, enemyPosition));

        return new GameClientState(gameId, stateId, map, player, enemy);
    }

    private void updateEnemy(Player newEnemy) {
        if (enemy.isEmpty()) {
            enemy = Optional.of(newEnemy);

            logger.info("Player {} joined the game", newEnemy);
        } else {
            enemy.get().update(newEnemy);
        }
    }

    /**
     * Update the game client's game state with the new server-source game state.
     *
     * @param newState server-sourced game state
     * @return whether there were any changes in the game's state
     */
    public boolean update(GameClientState newState) {
        // Return early, if the new state has not changed
        if (stateId.equals(newState.stateId)) {
            return false;
        }

        player.update(newState.player);
        newState.enemy.ifPresent(this::updateEnemy);
        map.update(newState.map, player.getPosition());

        return true;
    }

    public GameMap getMap() {
        return map;
    }

    public Optional<Position> getMapNodePosition(Predicate<GameMapNode> predicate) {
        return map.getPositionsByMapNode(predicate).stream().findFirst();
    }

    public Position getPlayerPosition() {
        return player.getPosition();
    }

    private Position getEnemyPosition() {
        return enemy.orElseThrow().getPosition();
    }

    public boolean hasFullMap() {
        return map.isFullMap();
    }

    public boolean hasBothPlayers() {
        return enemy.isPresent();
    }

    public boolean shouldClientAct() {
        return player.shouldPlayerAct();
    }

    public boolean hasFoundTreasure() {
        return hasCollectedTreasure() || map.anyMapNodeMatch(GameMapNode::hasTreasure);
    }

    public boolean hasCollectedTreasure() {
        return player.hasTreasure();
    }

    public boolean hasFoundEnemyFort() {
        return hasClientWon() || map.anyMapNodeMatch(GameMapNode::hasEnemyFort);
    }

    public boolean hasClientWon() {
        return player.hasWon();
    }

    public boolean hasClientLost() {
        return player.hasLost();
    }

    public Optional<Player> getEnemy() {
        return enemy;
    }

    public String toString() {
        Position playerPosition = getPlayerPosition();
        Position enemyPosition = getEnemyPosition();

        return map.renderToString(Optional.of(playerPosition), Optional.of(enemyPosition));
    }
}
