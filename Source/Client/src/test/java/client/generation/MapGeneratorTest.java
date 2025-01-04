package client.generation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import client.map.GameMap;
import client.validation.HalfMapValidator;

class MapGeneratorTest {

    private static final int X_SIZE = 10;
    private static final int Y_SIZE = 5;
    private static final int MAP_SIZE = X_SIZE * Y_SIZE;

    private static final long STANDARD_START_SEED = 0xDEAD_FACADEL;

    private static final long EXPECTED_MAX_RUN_TIME_SECS = 5L;

    @Test
    @Timeout(EXPECTED_MAX_RUN_TIME_SECS)
    void HalfMapValidator_generateUntilValid_completesWithinExpectedRuntime() {
        MapGenerator mapGenerator = new MapGenerator(STANDARD_START_SEED);
        HalfMapValidator validator = new HalfMapValidator();

        GameMap map = mapGenerator.generateUntilValid(validator);

        // Dummy assertion to have at least one assertion in the test case
        assertEquals(MAP_SIZE, map.getSize(), "Game Map should have the expected size");
    }
}