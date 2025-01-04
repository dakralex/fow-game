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

class GameMapSizeValidatorTest {

    private static final int X_SIZE = 10;
    private static final int Y_SIZE = 5;

    private static final GameMapValidationRule validator = new GameMapSizeValidator();

    @Test
    void CorrectlySizedMap_validate_shouldMarkAsValid() {
        GameMap map = MapGenerationUtils.generateEmptyGameMap(X_SIZE, Y_SIZE);

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
                    arguments(X_SIZE, Y_SIZE - 1),
                    arguments(X_SIZE, Y_SIZE + 1),
                    arguments(X_SIZE - 1, Y_SIZE),
                    arguments(X_SIZE + 1, Y_SIZE),
                    // Full Map dimensions
                    arguments(2 * X_SIZE, 2 * Y_SIZE)
            );
        }
    }
}