package client.map;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PositionAreaTest {

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
}