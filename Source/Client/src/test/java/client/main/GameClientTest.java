package client.main;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import client.map.FortState;
import client.map.GameMap;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.Position;
import client.map.TerrainType;
import client.map.TreasureState;
import client.map.util.MapGenerationUtils;
import client.network.GameStateUpdater;
import client.player.Player;
import client.player.PlayerDetails;
import client.player.PlayerGameState;

class GameClientTest {

    private static final int HORIZONTAL_FULL_MAP_X_SIZE = 20;
    private static final int HORIZONTAL_FULL_MAP_Y_SIZE = 5;

    private static final String DUMMY_NAME = "Dummy";
    private static final String PLAYER_ID = "dummy01";
    private static final String ENEMY_ID = "dummy02";

    private static final PlayerDetails dummyPlayerDetails = new PlayerDetails(DUMMY_NAME,
                                                                              "Player",
                                                                              PLAYER_ID);
    private static final PlayerDetails dummyEnemyDetails = new PlayerDetails(DUMMY_NAME,
                                                                             "Enemy",
                                                                             ENEMY_ID);

    private static final Position playerFortPosition = new Position(0, 0);
    private static final Position treasurePosition = new Position(HORIZONTAL_FULL_MAP_X_SIZE / 2,
                                                                  HORIZONTAL_FULL_MAP_Y_SIZE / 2);
    private static final Position enemyFortPosition = new Position(HORIZONTAL_FULL_MAP_X_SIZE - 1,
                                                                   HORIZONTAL_FULL_MAP_Y_SIZE - 1);

    private static final Consumer<Map<Position, GameMapNode>> makeFinishedMap = mapNodes -> {
        mapNodes.putAll(Map.of(
                playerFortPosition,
                new GameMapNode(playerFortPosition,
                                TerrainType.GRASS,
                                FortState.PLAYER_FORT_PRESENT,
                                TreasureState.NO_TREASURE_PRESENT),
                treasurePosition,
                new GameMapNode(treasurePosition,
                                TerrainType.GRASS,
                                FortState.NO_FORT_PRESENT,
                                TreasureState.PLAYER_TREASURE_PRESENT),
                enemyFortPosition,
                new GameMapNode(enemyFortPosition,
                                TerrainType.GRASS,
                                FortState.ENEMY_FORT_PRESENT,
                                TreasureState.NO_TREASURE_PRESENT)
        ));
    };

    private static Player makeDummyPlayer(PlayerGameState playerState, Position position,
                                          boolean hasTreasure) {
        return new Player(PLAYER_ID, dummyPlayerDetails, playerState, position, hasTreasure);
    }

    private static Player makeDummyEnemy(PlayerGameState playerState, Position position,
                                         boolean hasTreasure) {
        return new Player(ENEMY_ID, dummyEnemyDetails, playerState, position, hasTreasure);
    }

    private static void assertFinish(GameClient gameClient) {
        assertTrue(gameClient.getCurrentState().hasClientWon(),
                   "Client's player should be marked as winner.");
        assertTrue(true, "Game client has ended execution");
    }

    @Test
    @Timeout(1L)
    void FinishedGameClientState_run_shouldRunThroughAllStages() {
        GameMap map = MapGenerationUtils.generateEmptyGameMap(HORIZONTAL_FULL_MAP_X_SIZE,
                                                              HORIZONTAL_FULL_MAP_Y_SIZE,
                                                              makeFinishedMap);
        Player player = makeDummyPlayer(PlayerGameState.WON, enemyFortPosition, true);
        Player enemy = makeDummyEnemy(PlayerGameState.LOST, new Position(0, 0), false);
        GameClientState state = new GameClientState("test0", "0", map, player, enemy);

        GameStateUpdater stateUpdater = mock();
        when(stateUpdater.pollGameState()).thenReturn(state);

        GameClient gameClient = new GameClient(state, stateUpdater);

        gameClient.run();

        assertFinish(gameClient);
    }

    @Test
    void PerfectKnowledge_run_shouldRunThroughAllStages() {
        String gameId = "test1";
        GameMap map = MapGenerationUtils.generateEmptyGameMap(HORIZONTAL_FULL_MAP_X_SIZE,
                                                              HORIZONTAL_FULL_MAP_Y_SIZE,
                                                              makeFinishedMap);
        Player player = makeDummyPlayer(PlayerGameState.MUST_ACT, playerFortPosition, false);
        Player enemy = makeDummyEnemy(PlayerGameState.MUST_WAIT, enemyFortPosition, false);
        GameClientState state = new GameClientState(gameId, "1", map, player, enemy);

        GameStateUpdater stateUpdater = mock();

        GameClient gameClient = new GameClient(state, stateUpdater, 0L);

        ArgumentCaptor<MapDirection> argumentCaptor = ArgumentCaptor.forClass(MapDirection.class);
        doNothing().when(stateUpdater).sendMapMove(argumentCaptor.capture());

        AtomicReference<Integer> counter = new AtomicReference<>(1);
        Collection<MapDirection> currentDirections = new ArrayList<>(2);
        when(stateUpdater.pollGameState()).then(invocation -> {
            MapDirection direction = argumentCaptor.getValue();
            currentDirections.add(direction);

            counter.getAndSet(counter.get() + 1);

            if (currentDirections.size() < 2) {
                return new GameClientState(gameId, String.valueOf(counter), map, player, enemy);
            } else {
                currentDirections.clear();
            }

            Position newPosition = state.getPlayerPosition().stepInDirection(direction);
            boolean hasTreasure = state.hasCollectedTreasure()
                    || newPosition.equals(treasurePosition);
            PlayerGameState playerState = newPosition.equals(enemyFortPosition) ?
                    PlayerGameState.WON : PlayerGameState.MUST_ACT;
            Player newPlayer = makeDummyPlayer(playerState, newPosition, hasTreasure);

            return new GameClientState(gameId, String.valueOf(counter), map, newPlayer, enemy);
        });

        gameClient.run();

        assertFinish(gameClient);
    }
}