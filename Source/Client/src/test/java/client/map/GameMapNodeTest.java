package client.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameMapNodeTest {

    private static void assertPlayerFortState(GameMapNode mapNode) {
        assertTrue(mapNode.hasPlayerFort(), "Player fort should be present here");
        assertFalse(mapNode.hasEnemyFort(), "Enemy fort shouldn't be present here");
        assertFalse(mapNode.isUnvisited(), "Player fort should be marked as visited");
    }

    private static void assertPlayerTreasureState(GameMapNode mapNode) {
        assertTrue(mapNode.hasTreasure(), "Treasure should be present here");
    }

    private static void assertUnknownIntelligenceUnvisitedState(GameMapNode mapNode) {
        assertTrue(mapNode.isUnvisited(),
                   "GameMapNode with unknown intelligence should be marked as unvisited");
    }

    @Test
    void unreliableIntelligence_resetIntelligence_shouldResetToUnknownIntelligence() {
        // Assume this mapNode is a server-sourced GameMapNode about
        // to be freshly added to the GameMap
        GameMapNode mapNode = new GameMapNode(Position.originPosition,
                                              TerrainType.GRASS,
                                              FortState.NO_FORT_PRESENT,
                                              TreasureState.NO_TREASURE_PRESENT);

        assertFalse(mapNode.isUnvisited(),
                    "GameMapNode with unreliable intelligence should be marked as visited");

        mapNode.resetIntelligence();

        assertTrue(mapNode.isUnvisited(),
                   "GameMapNode with unreliable intelligence should be marked unvisited after an intelligence reset");
    }

    @Test
    void unknownIntelligence_resetIntelligence_shouldStayInUnknownIntelligence() {
        GameMapNode mapNode = new GameMapNode(Position.originPosition,
                                              TerrainType.GRASS,
                                              FortState.UNKNOWN,
                                              TreasureState.UNKNOWN);

        assertUnknownIntelligenceUnvisitedState(mapNode);

        mapNode.resetIntelligence();

        assertTrue(mapNode.isUnvisited(),
                   "GameMapNode with unknown intelligence should stay unvisited after an intelligence reset");
    }

    @Test
    void unknownIntelligence_update_shouldUpdateToNewStateIfVisible() {
        GameMapNode targetMapNode = new GameMapNode(Position.originPosition,
                                                    TerrainType.GRASS,
                                                    FortState.UNKNOWN,
                                                    TreasureState.UNKNOWN);
        GameMapNode newMapNode = new GameMapNode(Position.originPosition,
                                                 TerrainType.GRASS,
                                                 FortState.PLAYER_FORT_PRESENT,
                                                 TreasureState.PLAYER_TREASURE_PRESENT);

        assertUnknownIntelligenceUnvisitedState(targetMapNode);

        targetMapNode.update(newMapNode, true);

        assertAll(
                "Update of visible GameMapNode with previously unknown intelligence should change to new intelligence states",
                () -> assertFalse(targetMapNode.isUnvisited()),
                () -> assertTrue(targetMapNode.hasPlayerFort()),
                () -> assertTrue(targetMapNode.hasTreasure()));
    }

    @Test
    void unknownIntelligence_update_shouldUpdateToNewStateIfNotVisible() {
        GameMapNode targetMapNode = new GameMapNode(Position.originPosition,
                                                    TerrainType.GRASS,
                                                    FortState.UNKNOWN,
                                                    TreasureState.UNKNOWN);
        GameMapNode newMapNode = new GameMapNode(Position.originPosition,
                                                 TerrainType.GRASS,
                                                 FortState.PLAYER_FORT_PRESENT,
                                                 TreasureState.PLAYER_TREASURE_PRESENT);

        assertUnknownIntelligenceUnvisitedState(targetMapNode);

        targetMapNode.update(newMapNode, false);

        assertAll(
                "Update of not visible GameMapNode with previously unknown intelligence should not change to new intelligence states",
                () -> assertTrue(targetMapNode.isUnvisited()),
                () -> assertFalse(targetMapNode.hasPlayerFort()),
                () -> assertFalse(targetMapNode.hasTreasure()));
    }

    @Test
    void playerFort_resetIntelligence_shouldNotReset() {
        GameMapNode playerMapNode = new GameMapNode(Position.originPosition,
                                                    TerrainType.GRASS,
                                                    FortState.PLAYER_FORT_PRESENT,
                                                    TreasureState.UNKNOWN);

        assertPlayerFortState(playerMapNode);

        playerMapNode.resetIntelligence();

        assertTrue(playerMapNode.hasPlayerFort(),
                   "Player fort should still have player fort after an intelligence reset");
        assertFalse(playerMapNode.isUnvisited(),
                    "Player fort should stay marked as visited after an intelligence reset");
    }

    @Test
    void playerFort_update_shouldNotUpdateToNewState() {
        GameMapNode targetMapNode = new GameMapNode(Position.originPosition,
                                                    TerrainType.GRASS,
                                                    FortState.PLAYER_FORT_PRESENT,
                                                    TreasureState.NO_TREASURE_PRESENT);
        GameMapNode newMapNode = new GameMapNode(Position.originPosition,
                                                 TerrainType.GRASS,
                                                 FortState.ENEMY_FORT_PRESENT,
                                                 TreasureState.PLAYER_TREASURE_PRESENT);

        assertPlayerFortState(targetMapNode);

        targetMapNode.update(newMapNode, true);

        assertAll("Update of visible GameMapNode with player fort should not change anymore",
                  () -> assertFalse(targetMapNode.isUnvisited()),
                  () -> assertTrue(targetMapNode.hasPlayerFort()),
                  () -> assertFalse(targetMapNode.hasEnemyFort()),
                  () -> assertFalse(targetMapNode.hasTreasure()));
    }

    @Test
    void enemyFort_resetIntelligence_shouldNotReset() {
        GameMapNode enemyMapNode = new GameMapNode(Position.originPosition,
                                                   TerrainType.GRASS,
                                                   FortState.ENEMY_FORT_PRESENT,
                                                   TreasureState.UNKNOWN);

        assertTrue(enemyMapNode.hasEnemyFort(), "Enemy fort should be present here");
        assertFalse(enemyMapNode.hasPlayerFort(), "Player fort shouldn't be present here");
        assertFalse(enemyMapNode.isUnvisited(), "Enemy fort should be marked as visited");

        enemyMapNode.resetIntelligence();

        assertTrue(enemyMapNode.hasEnemyFort(),
                   "Enemy fort should still have enemy fort after an intelligence reset");
        assertFalse(enemyMapNode.isUnvisited(),
                    "Enemy fort should stay marked as visited after an intelligence reset");
    }

    @Test
    void playerTreasure_resetIntelligence_shouldNotReset() {
        GameMapNode playerTreasureMapNode = new GameMapNode(Position.originPosition,
                                                            TerrainType.GRASS,
                                                            FortState.UNKNOWN,
                                                            TreasureState.PLAYER_TREASURE_PRESENT);

        assertPlayerTreasureState(playerTreasureMapNode);

        playerTreasureMapNode.resetIntelligence();

        assertTrue(playerTreasureMapNode.hasTreasure(),
                   "Treasure GameMapNode should still have treasure after an intelligence reset");
    }

    @Test
    void playerTreasure_update_shouldNotUpdateToNewState() {
        GameMapNode targetMapNode = new GameMapNode(Position.originPosition,
                                                    TerrainType.GRASS,
                                                    FortState.NO_FORT_PRESENT,
                                                    TreasureState.PLAYER_TREASURE_PRESENT);
        GameMapNode newMapNode = new GameMapNode(Position.originPosition,
                                                 TerrainType.GRASS,
                                                 FortState.ENEMY_FORT_PRESENT,
                                                 TreasureState.NO_TREASURE_PRESENT);

        assertPlayerTreasureState(targetMapNode);

        targetMapNode.update(newMapNode, true);

        assertAll("Update of visible GameMapNode with player treasure should not change anymore",
                  () -> assertFalse(targetMapNode.isUnvisited()),
                  () -> assertFalse(targetMapNode.hasPlayerFort()),
                  () -> assertFalse(targetMapNode.hasEnemyFort()),
                  () -> assertTrue(targetMapNode.hasTreasure()));
    }
}