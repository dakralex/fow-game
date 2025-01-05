package client.validation;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.stream.Stream;

import client.map.GameMap;
import client.map.util.MapGenerationUtils;
import client.validation.util.NotificationAssertUtils;

class GameMapDimensionValidatorTest {

    private static final int HALF_MAP_X_SIZE = 10;
    private static final int HALF_MAP_Y_SIZE = 5;

    private static final GameMapValidationRule validator = new GameMapDimensionValidator();

    @Test
    void CorrectlySizedMap_validate_shouldMarkAsValid() {
        GameMap map = MapGenerationUtils.generateEmptyGameMap(HALF_MAP_X_SIZE, HALF_MAP_Y_SIZE);

        NotificationAssertUtils.assertNoViolation(map, validator);
    }

    @ParameterizedTest
    @ArgumentsSource(WeirdlySizedMapArgumentsProvider.class)
    void WeirdlySizedMap_validate_shouldMarkAsInvalid(int mapXSize, int mapYSize) {
        GameMap map = MapGenerationUtils.generateEmptyGameMap(mapXSize, mapYSize);

        NotificationAssertUtils.assertSomeViolation(map, validator);
    }

    private static class WeirdlySizedMapArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    // One-off Half Map dimensions
                    arguments(HALF_MAP_X_SIZE, HALF_MAP_Y_SIZE - 1),
                    arguments(HALF_MAP_X_SIZE, HALF_MAP_Y_SIZE + 1),
                    arguments(HALF_MAP_X_SIZE - 1, HALF_MAP_Y_SIZE),
                    arguments(HALF_MAP_X_SIZE + 1, HALF_MAP_Y_SIZE),
                    // Full Map dimensions
                    arguments(2 * HALF_MAP_X_SIZE, 2 * HALF_MAP_Y_SIZE)
            );
        }
    }
}