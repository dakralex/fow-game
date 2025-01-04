package client.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.function.Function;
import java.util.function.Predicate;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.PositionArea;
import client.map.TerrainType;
import client.map.util.MapGenerationUtils;
import client.validation.util.NotificationAssertUtils;

class GameMapBorderAccessibilityValidatorTest {

    private static final int X_SIZE = 10;
    private static final int Y_SIZE = 5;

    private static final GameMapValidationRule validator = new GameMapBorderAccessibilityValidator();

    private static final Function<GameMapNode, GameMapNode> makeInaccessible =
            mapNode -> new GameMapNode(mapNode.getPosition(), TerrainType.WATER);

    @Test
    void CompletelyAccessibleBorder_validate_shouldMarkAsValid() {
        GameMap map = MapGenerationUtils.generateEmptyGameMap(X_SIZE, Y_SIZE);

        NotificationAssertUtils.assertNoViolation(map, validator);
    }

    @ParameterizedTest
    @EnumSource(MapDirection.class)
    void CompletelyInaccessibleBorder_validate_shouldMarkAsInvalid(MapDirection borderDirection) {
        PositionArea mapArea = new PositionArea(0, 0, X_SIZE, Y_SIZE);
        Predicate<GameMapNode> isOnBorder = mapNode ->
                mapArea.getBorderPredicate(borderDirection).test(mapNode.getPosition());

        GameMap map = MapGenerationUtils.generateEmptyGameMap(X_SIZE, Y_SIZE, makeInaccessible, isOnBorder);

        NotificationAssertUtils.assertSomeViolation(map, validator);
    }
}