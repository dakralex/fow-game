package client.validation;

import client.map.GameMap;
import client.map.PositionArea;

public class GameMapPositionsValidator implements GameMapValidationRule {

    private static final int X_SIZE = 10;
    private static final int Y_SIZE = 5;

    @Override
    public boolean validate(GameMap map) {
        PositionArea area = new PositionArea(0, 0, X_SIZE, Y_SIZE);

        return map.getPositions().stream().allMatch(area::isInside);
    }
}
