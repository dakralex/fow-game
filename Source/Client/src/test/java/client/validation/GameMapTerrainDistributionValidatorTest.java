package client.validation;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Queue;
import java.util.function.Function;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.TerrainType;
import client.map.util.MapGenerationUtils;
import client.validation.util.NotificationAssertUtils;

class GameMapTerrainDistributionValidatorTest {

    private static final int HALF_MAP_X_SIZE = 10;
    private static final int HALF_MAP_Y_SIZE = 5;
    private static final int HALF_MAP_SIZE = HALF_MAP_X_SIZE * HALF_MAP_Y_SIZE;

    private static final int GRASS_MIN_AMOUNT = 24;
    private static final int MOUNTAIN_MIN_AMOUNT = 5;
    private static final int WATER_MIN_AMOUNT = 7;

    private static final int TERRAIN_MIN_AMOUNT = GRASS_MIN_AMOUNT + MOUNTAIN_MIN_AMOUNT + WATER_MIN_AMOUNT;

    private static final GameMapValidationRule validator = new GameMapTerrainDistributionValidator();

    private static Queue<TerrainType> generateTerrainTypes(int grassAmount,
                                                           int mountainAmount,
                                                           int waterAmount) {
        Queue<TerrainType> terrainTypes = new ArrayDeque<>(TERRAIN_MIN_AMOUNT);

        terrainTypes.addAll(Collections.nCopies(grassAmount, TerrainType.GRASS));
        terrainTypes.addAll(Collections.nCopies(mountainAmount, TerrainType.MOUNTAIN));
        terrainTypes.addAll(Collections.nCopies(waterAmount, TerrainType.WATER));

        return terrainTypes;
    }

    private static Function<GameMapNode, GameMapNode> placeTerrains(
            Queue<TerrainType> terrainTypes) {
        return mapNode -> {
            mapNode.setTerrainType(terrainTypes.remove());

            return mapNode;
        };
    }

    private static final Function<GameMapNode, GameMapNode> placeExactTerrains =
            placeTerrains(generateTerrainTypes(GRASS_MIN_AMOUNT,
                                               MOUNTAIN_MIN_AMOUNT,
                                               WATER_MIN_AMOUNT));

    private static Function<GameMapNode, GameMapNode> placeNotEnoughGrassTerrain() {
        // One off amount of grass terrain instances
        int grassAmount = GRASS_MIN_AMOUNT - 1;
        // Remaining amount that needs to be filled with something else than grass terrain
        int remainingSpace = HALF_MAP_SIZE - grassAmount - MOUNTAIN_MIN_AMOUNT - WATER_MIN_AMOUNT;

        return placeTerrains(generateTerrainTypes(grassAmount,
                                                  MOUNTAIN_MIN_AMOUNT + remainingSpace,
                                                  WATER_MIN_AMOUNT));
    }

    // Simulates one off amount of mountain terrain instances
    private static final Function<GameMapNode, GameMapNode> placeNotEnoughMountainTerrain =
            placeTerrains(generateTerrainTypes(GRASS_MIN_AMOUNT,
                                               MOUNTAIN_MIN_AMOUNT - 1,
                                               WATER_MIN_AMOUNT));

    // Simulates one off amount of water terrain instances
    private static final Function<GameMapNode, GameMapNode> placeNotEnoughWaterTerrain =
            placeTerrains(generateTerrainTypes(GRASS_MIN_AMOUNT,
                                               MOUNTAIN_MIN_AMOUNT,
                                               WATER_MIN_AMOUNT - 1));

    @Test
    void ExactTerrainDistributed_validate_shouldMarkAsValid() {
        GameMap map = MapGenerationUtils.generateEmptyGameMap(HALF_MAP_X_SIZE,
                                                              HALF_MAP_Y_SIZE,
                                                              placeExactTerrains,
                                                              GameMapNode::isLootable,
                                                              TERRAIN_MIN_AMOUNT);

        NotificationAssertUtils.assertNoViolation(map, validator);
    }

    @Test
    void NotEnoughGrass_validate_shouldMarkAsInvalid() {
        GameMap map = MapGenerationUtils.generateEmptyGameMap(HALF_MAP_X_SIZE,
                                                              HALF_MAP_Y_SIZE,
                                                              placeNotEnoughGrassTerrain(),
                                                              GameMapNode::isLootable,
                                                              HALF_MAP_SIZE);

        NotificationAssertUtils.assertSomeViolation(map, validator);
    }

    @Test
    void NotEnoughMountain_validate_shouldMarkAsInvalid() {
        GameMap map = MapGenerationUtils.generateEmptyGameMap(HALF_MAP_X_SIZE,
                                                              HALF_MAP_Y_SIZE,
                                                              placeNotEnoughMountainTerrain,
                                                              GameMapNode::isLootable,
                                                              TERRAIN_MIN_AMOUNT - 1);

        NotificationAssertUtils.assertSomeViolation(map, validator);
    }

    @Test
    void NotEnoughWater_validate_shouldMarkAsInvalid() {
        GameMap map = MapGenerationUtils.generateEmptyGameMap(HALF_MAP_X_SIZE,
                                                              HALF_MAP_Y_SIZE,
                                                              placeNotEnoughWaterTerrain,
                                                              GameMapNode::isLootable,
                                                              TERRAIN_MIN_AMOUNT - 1);

        NotificationAssertUtils.assertSomeViolation(map, validator);
    }
}