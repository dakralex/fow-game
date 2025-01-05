package client.generation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import client.map.TerrainType;

class TerrainDistributionGeneratorTest {

    private static final int GRASS_MIN_AMOUNT = 24;
    private static final int MOUNTAIN_MIN_AMOUNT = 5;
    private static final int WATER_MIN_AMOUNT = 7;

    private static final int MAP_MIN_SIZE = GRASS_MIN_AMOUNT + MOUNTAIN_MIN_AMOUNT + WATER_MIN_AMOUNT;

    private static final int HALF_MAP_X_SIZE = 10;
    private static final int HALF_MAP_Y_SIZE = 5;
    private static final int HALF_MAP_SIZE = HALF_MAP_X_SIZE * HALF_MAP_Y_SIZE;

    private static void assertEnoughTerrainTypes(Collection<TerrainType> terrainTypes) {
        int grassAmount = Collections.frequency(terrainTypes, TerrainType.GRASS);
        int mountainAmount = Collections.frequency(terrainTypes, TerrainType.MOUNTAIN);
        int waterAmount = Collections.frequency(terrainTypes, TerrainType.WATER);

        assertTrue(grassAmount >= GRASS_MIN_AMOUNT,
                   "There are less grass terrain instances than required");
        assertTrue(mountainAmount >= MOUNTAIN_MIN_AMOUNT,
                   "There are less mountain terrain instances than required");
        assertTrue(waterAmount >= WATER_MIN_AMOUNT,
                   "There are less water terrain instances than required");
    }

    @Test
    void NotEnoughSpace_generateTerrainQueue_shouldFail() {
        TerrainDistributionGenerator terrainGenerator = new TerrainDistributionGenerator();

        int notEnough = MAP_MIN_SIZE - 1;
        assertThrows(IllegalArgumentException.class,
                     () -> terrainGenerator.generateTerrainQueue(notEnough));
    }

    @Test
    void GameHalfMapSpace_generateTerrainQueue_shouldCorrectlyOutputTerrainDistribution() {
        TerrainDistributionGenerator terrainGenerator = new TerrainDistributionGenerator();

        assertEnoughTerrainTypes(terrainGenerator.generateTerrainQueue(HALF_MAP_SIZE));
    }

    @Test
    void BarelyEnoughSpace_generateTerrainQueue_shouldCorrectlyOutputTerrainDistribution() {
        TerrainDistributionGenerator terrainGenerator = new TerrainDistributionGenerator();

        assertEnoughTerrainTypes(terrainGenerator.generateTerrainQueue(MAP_MIN_SIZE));
    }
}