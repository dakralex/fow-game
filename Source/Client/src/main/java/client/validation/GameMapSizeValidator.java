package client.validation;

import client.map.GameMap;

public class GameMapSizeValidator implements GameMapValidationRule {

    private static final int X_SIZE = 10;
    private static final int Y_SIZE = 5;
    private static final int MAP_SIZE = X_SIZE * Y_SIZE;

    @Override
    public boolean validate(GameMap map) {
        return map.getSize() == MAP_SIZE;
    }
}
