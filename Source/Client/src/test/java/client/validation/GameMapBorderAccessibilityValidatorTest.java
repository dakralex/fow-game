package client.validation;

import static client.map.util.MapGenerationUtils.makeInaccessible;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.function.Predicate;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.PositionArea;
import client.map.util.MapGenerationUtils;
import client.validation.util.NotificationAssertUtils;

class GameMapBorderAccessibilityValidatorTest {

    private static final int HALF_MAP_X_SIZE = 10;
    private static final int HALF_MAP_Y_SIZE = 5;

    private static final GameMapValidationRule validator = new GameMapBorderAccessibilityValidator();

    @Test
    void CompletelyAccessibleBorder_validate_shouldMarkAsValid() {
        GameMap map = MapGenerationUtils.generateEmptyGameMap(HALF_MAP_X_SIZE, HALF_MAP_Y_SIZE);

        NotificationAssertUtils.assertNoViolation(map, validator);
    }

    @ParameterizedTest
    @EnumSource(MapDirection.class)
    void CompletelyInaccessibleBorder_validate_shouldMarkAsInvalid(MapDirection borderDirection) {
        PositionArea mapArea = new PositionArea(0, 0, HALF_MAP_X_SIZE, HALF_MAP_Y_SIZE);
        Predicate<GameMapNode> isOnBorder = mapNode ->
                mapArea.getBorderPredicate(borderDirection).test(mapNode.getPosition());

        GameMap map = MapGenerationUtils.generateEmptyGameMap(HALF_MAP_X_SIZE,
                                                              HALF_MAP_Y_SIZE,
                                                              makeInaccessible,
                                                              isOnBorder);

        NotificationAssertUtils.assertSomeViolation(map, validator);
    }
}