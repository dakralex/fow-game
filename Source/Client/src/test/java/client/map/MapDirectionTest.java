package client.map;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class MapDirectionTest {

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
}