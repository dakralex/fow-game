package client.validation;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Consumer;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.Position;
import client.map.TerrainType;
import client.map.util.MapGenerationUtils;
import client.validation.util.NotificationAssertUtils;

class GameMapFortPlacementValidatorTest {

    private static final int HALF_MAP_X_SIZE = 10;
    private static final int HALF_MAP_Y_SIZE = 5;

    private static final Consumer<Map<Position, GameMapNode>> placeTwoPlayerForts = mapNodes -> {
        mapNodes.get(new Position(0, HALF_MAP_Y_SIZE / 2)).placePlayerFort();
        mapNodes.get(new Position(HALF_MAP_X_SIZE / 2, 0)).placePlayerFort();
    };

    private static final Consumer<Map<Position, GameMapNode>> placeEnemyFort =
            mapNodes -> mapNodes.get(new Position(5, 0)).placeEnemyFort();

    private static final Consumer<Map<Position, GameMapNode>> placeWaterPlayerFort = mapNodes -> {
        GameMapNode fortNode = mapNodes.get(new Position(0, 4));

        fortNode.placePlayerFort();
        fortNode.setTerrainType(TerrainType.MOUNTAIN);
    };

    private static final GameMapValidationRule validator = new GameMapFortPlacementValidator();

    @Test
    void PlayerFortPlaced_validate_shouldMarkAsValid() {
        GameMap map = MapGenerationUtils.generateEmptyGameMap(HALF_MAP_X_SIZE,
                                                              HALF_MAP_Y_SIZE,
                                                              MapDirection.WEST);

        NotificationAssertUtils.assertNoViolation(map, validator);
    }

    @Test
    void MultipleFortsPlaced_validate_shouldMarkAsInvalid() {
        GameMap map = MapGenerationUtils.generateEmptyGameMap(HALF_MAP_X_SIZE,
                                                              HALF_MAP_Y_SIZE,
                                                              placeTwoPlayerForts);

        NotificationAssertUtils.assertSomeViolation(map, validator);
    }

    @Test
    void NoFortPlaced_validate_shouldMarkAsInvalid() {
        GameMap map = MapGenerationUtils.generateEmptyGameMap(HALF_MAP_X_SIZE, HALF_MAP_Y_SIZE);

        NotificationAssertUtils.assertSomeViolation(map, validator);
    }

    @Test
    void EnemyFortPlaced_validate_shouldMarkAsInvalid() {
        GameMap map = MapGenerationUtils.generateEmptyGameMap(HALF_MAP_X_SIZE,
                                                              HALF_MAP_Y_SIZE,
                                                              placeEnemyFort);

        NotificationAssertUtils.assertSomeViolation(map, validator);
    }

    @Test
    void NoGrassFortPlaced_validate_shouldMarkAsInvalid() {
        GameMap map = MapGenerationUtils.generateEmptyGameMap(HALF_MAP_X_SIZE,
                                                              HALF_MAP_Y_SIZE,
                                                              placeWaterPlayerFort);

        NotificationAssertUtils.assertSomeViolation(map, validator);
    }
}