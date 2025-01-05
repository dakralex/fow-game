package client.map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.stream.Stream;

class MapDirectionTest {

    @ParameterizedTest
    @ArgumentsSource(DifferentialsMapDirectionArgumentsProvider.class)
    void MapDirection_fromDifferentials_shouldReturnExpectedDirection(int dx, int dy,
                                                                      MapDirection direction) {
        assertEquals(direction,
                     MapDirection.fromDifferentials(dx, dy),
                     "Direction should equal the expected direction from the differential values");
    }

    @ParameterizedTest
    @EnumSource(MapDirection.class)
    void MapDirection_getOpposite_shouldReturnExpectedDirection(MapDirection direction) {
        assertEquals(switch (direction) {
            case EAST -> MapDirection.WEST;
            case NORTH -> MapDirection.SOUTH;
            case SOUTH -> MapDirection.NORTH;
            case WEST -> MapDirection.EAST;
        }, direction.getOpposite(), "Direction should be the opposite of each other");
    }

    @ParameterizedTest
    @EnumSource(MapDirection.class)
    void MapDirection_getPerpendicular_shouldReturnExpectedDirection(MapDirection direction) {
        assertEquals(switch (direction) {
            case EAST -> MapDirection.SOUTH;
            case NORTH -> MapDirection.EAST;
            case SOUTH -> MapDirection.WEST;
            case WEST -> MapDirection.NORTH;
        }, direction.getPerpendicular(), "Direction should be perpendicular to the right");
    }

    private static class DifferentialsMapDirectionArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    arguments(-1, 0, MapDirection.WEST),
                    arguments(1, 0, MapDirection.EAST),
                    arguments(0, 1, MapDirection.SOUTH),
                    arguments(0, -1, MapDirection.NORTH)
            );
        }
    }
}