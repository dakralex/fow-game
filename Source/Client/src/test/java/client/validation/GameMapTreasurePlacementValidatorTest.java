package client.validation;

import org.junit.jupiter.api.Test;

import java.util.function.Function;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.util.MapGenerationUtils;
import client.validation.util.NotificationAssertUtils;

class GameMapTreasurePlacementValidatorTest {

    private static final int X_SIZE = 10;
    private static final int Y_SIZE = 5;

    private static final GameMapValidationRule validator = new GameMapTreasurePlacementValidator();

    private static final Function<GameMapNode, GameMapNode> placePlayerTreasure =
            mapNode -> {
                mapNode.placePlayerTreasure();
                return mapNode;
            };

    @Test
    void NoTreasurePlaced_validate_shouldMarkAsValid() {
        GameMap map = MapGenerationUtils.generateEmptyGameMap(X_SIZE, Y_SIZE);

        NotificationAssertUtils.assertNoViolation(map, validator);
    }

    @Test
    void OneTreasurePlaced_validate_shouldMarkAsInvalid() {
        GameMap map = MapGenerationUtils.generateEmptyGameMap(X_SIZE,
                                                              Y_SIZE,
                                                              placePlayerTreasure,
                                                              GameMapNode::isLootable,
                                                              1L);

        NotificationAssertUtils.assertSomeViolation(map, validator);
    }

    @Test
    void TwoTreasuresPlaced_validate_shouldMarkAsInvalid() {
        GameMap map = MapGenerationUtils.generateEmptyGameMap(X_SIZE,
                                                              Y_SIZE,
                                                              placePlayerTreasure,
                                                              GameMapNode::isLootable,
                                                              2L);

        NotificationAssertUtils.assertSomeViolation(map, validator);
    }
}