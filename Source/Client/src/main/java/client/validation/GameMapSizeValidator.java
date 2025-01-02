package client.validation;

import client.map.GameMap;

public class GameMapSizeValidator implements GameMapValidationRule {

    private static final int X_SIZE = 10;
    private static final int Y_SIZE = 5;
    private static final int MAP_SIZE = X_SIZE * Y_SIZE;

    @Override
    public void validate(GameMap map, Notification<GameMapValidationRule> note) {
        if (map.getSize() != MAP_SIZE) {
            note.addEntry(this,
                          String.format("Game map does contain less or more than %d fields",
                                        MAP_SIZE));
        }
    }
}
