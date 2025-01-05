package client.map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collection;
import java.util.stream.Stream;

class PositionAreaTest {

    private static final int HALF_MAP_X_SIZE = 10;
    private static final int HALF_MAP_Y_SIZE = 5;
    private static final int HALF_MAP_SIZE = HALF_MAP_X_SIZE * HALF_MAP_Y_SIZE;

    @ParameterizedTest
    @CsvSource({"5,5", "10,10", "5,10", "10, 5", "7,7"})
    void PositionArea_isOutside_shouldReturnFalseForPositionsInsideArea(int x, int y) {
        PositionArea area = new PositionArea(5, 5, 10, 10);

        assertFalse(area.isOutside(new Position(x, y)), "Position should be considered as inside");
    }

    @ParameterizedTest
    @CsvSource({"0,0", "4,4", "15,15", "4,8", "8,4"})
    void PositionArea_isOutside_shouldReturnTrueForPositionsOutsideArea(int x, int y) {
        PositionArea area = new PositionArea(5, 5, 10, 10);

        assertTrue(area.isOutside(new Position(x, y)), "Position should be considered as outside");
    }

    @ParameterizedTest
    @ArgumentsSource(HalfMapArgumentProvider.class)
    void HalfMaps_intoPositionStream_shouldOutputCompleteHalfMapPositionStream(int x, int y,
                                                                               int width,
                                                                               int height) {
        PositionArea area = new PositionArea(x, y, width, height);
        Collection<Position> areaPositions = area.intoPositionStream().toList();

        assertEquals(HALF_MAP_SIZE,
                     areaPositions.size(),
                     "HalfMap should contain the excepted GameMapNodes");

        int x1 = x + width - 1;
        int y1 = y + width - 1;

        Collection<Position> positionsOutsideXBound = areaPositions.stream()
                .filter(position -> position.x() < x || position.x() > x1)
                .toList();
        Collection<Position> positionsOutsideYBound = areaPositions.stream()
                .filter(position -> position.y() < y || position.y() > y1)
                .toList();

        assertTrue(positionsOutsideXBound.isEmpty(),
                   "Positions should be in the x coordinate range");
        assertTrue(positionsOutsideYBound.isEmpty(),
                   "Positions should be in the y coordinate range");
    }

    @ParameterizedTest
    @CsvSource({"5,5", "5,14", "14,5", "14,14"})
    void PositionArea_isCorner_shouldReturnTrueForCorners(int x, int y) {
        PositionArea area = new PositionArea(5, 5, 10, 10);

        assertTrue(area.isCorner(new Position(x, y)), "Position should be identified as a corner");
    }

    @ParameterizedTest
    @CsvSource({"4,4", "6,6", "4,15", "6,13", "15,4", "13,6", "13,13", "15,15"})
    void PositionArea_isCorner_shouldReturnFalseForAnythingOtherThanCorners(int x, int y) {
        PositionArea area = new PositionArea(5, 5, 10, 10);

        assertFalse(area.isCorner(new Position(x, y)), "Position should not be identified as a corner");
    }

    private static class HalfMapArgumentProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Stream.of(
                    // Horizontal west / Vertical north half map
                    arguments(0, 0, HALF_MAP_X_SIZE, HALF_MAP_Y_SIZE),
                    // Vertical south half map
                    arguments(0, HALF_MAP_Y_SIZE, HALF_MAP_X_SIZE, HALF_MAP_Y_SIZE),
                    // Horizontal east half map
                    arguments(HALF_MAP_X_SIZE, 0, HALF_MAP_X_SIZE, HALF_MAP_Y_SIZE)
            );
        }
    }
}