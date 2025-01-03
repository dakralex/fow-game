package client.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameMapNodeTest {

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

        assertTrue(mapNode.isUnvisited(),
                   "GameMapNode with unknown intelligence should be marked as unvisited");

        mapNode.resetIntelligence();

        assertTrue(mapNode.isUnvisited(),
                   "GameMapNode with unknown intelligence should stay unvisited after an intelligence reset");
    }

    @Test
    void playerFort_resetIntelligence_shouldNotReset() {
        GameMapNode playerMapNode = new GameMapNode(Position.originPosition,
                                                    TerrainType.GRASS,
                                                    FortState.PLAYER_FORT_PRESENT,
                                                    TreasureState.UNKNOWN);

        assertTrue(playerMapNode.hasPlayerFort(), "Player fort should be present here");
        assertFalse(playerMapNode.hasEnemyFort(), "Enemy fort shouldn't be present here");
        assertFalse(playerMapNode.isUnvisited(), "Player fort should be marked as visited");

        playerMapNode.resetIntelligence();

        assertTrue(playerMapNode.hasPlayerFort(),
                   "Player fort should still have player fort after an intelligence reset");
        assertFalse(playerMapNode.isUnvisited(),
                    "Player fort should stay marked as visited after an intelligence reset");
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

        assertTrue(playerTreasureMapNode.hasTreasure(), "Treasure should be present here");

        playerTreasureMapNode.resetIntelligence();

        assertTrue(playerTreasureMapNode.hasTreasure(),
                   "Treasure GameMapNode should still have treasure after an intelligence reset");
    }
}