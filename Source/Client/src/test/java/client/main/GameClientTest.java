package client.main;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Map;
import java.util.function.Consumer;

import client.map.FortState;
import client.map.GameMap;
import client.map.GameMapNode;
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

    private static final PlayerDetails dummyPlayerDetails = new PlayerDetails("Dummy",
                                                                              "Player",
                                                                              "dummy01");
    private static final PlayerDetails dummyEnemyDetails = new PlayerDetails("Dummy",
                                                                             "Enemy",
                                                                             "dummy02");

    private static final Consumer<Map<Position, GameMapNode>> makeFinishedMap = mapNodes -> {
        Position enemyFortPosition = new Position(HORIZONTAL_FULL_MAP_X_SIZE - 1,
                                                  HORIZONTAL_FULL_MAP_Y_SIZE - 1);

        mapNodes.putAll(Map.of(
                enemyFortPosition,
                new GameMapNode(enemyFortPosition,
                                TerrainType.GRASS,
                                FortState.ENEMY_FORT_PRESENT,
                                TreasureState.NO_TREASURE_PRESENT)
        ));
    };

    private static Player makeDummyPlayer(PlayerGameState playerState, Position position,
                                          boolean hasTreasure) {
        return new Player("dummy01", dummyPlayerDetails, playerState, position, hasTreasure);
    }

    private static Player makeDummyEnemy(PlayerGameState playerState, Position position,
                                         boolean hasTreasure) {
        return new Player("dummy02", dummyEnemyDetails, playerState, position, hasTreasure);
    }

    @Test
    @Timeout(1L)
    void FinishedGameClientState_run_shouldRunThroughAllStages() {
        GameMap map = MapGenerationUtils.generateEmptyGameMap(HORIZONTAL_FULL_MAP_X_SIZE,
                                                              HORIZONTAL_FULL_MAP_Y_SIZE,
                                                              makeFinishedMap);
        Position enemyFortPosition = new Position(HORIZONTAL_FULL_MAP_X_SIZE - 1,
                                                  HORIZONTAL_FULL_MAP_Y_SIZE - 1);

        Player player = makeDummyPlayer(PlayerGameState.WON, enemyFortPosition, true);
        Player enemy = makeDummyEnemy(PlayerGameState.LOST, new Position(0, 0), false);
        GameClientState state = new GameClientState("test0", "0", map, player, enemy);

        GameStateUpdater stateUpdater = mock();
        when(stateUpdater.pollGameState()).thenReturn(state);

        Runnable gameClient = new GameClient(state, stateUpdater);

        gameClient.run();

        assertTrue(true, "Game Client has reached its end");
    }
}