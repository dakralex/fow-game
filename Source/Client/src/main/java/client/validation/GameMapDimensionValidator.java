package client.validation;

import client.map.GameMap;
import client.map.PositionArea;

public class GameMapDimensionValidator implements GameMapValidationRule {

    private static final int X_SIZE = 10;
    private static final int Y_SIZE = 5;
    private static final int MAP_SIZE = X_SIZE * Y_SIZE;

    @Override
    public void validate(GameMap map, Notification<? super GameMapValidationRule> note) {
        PositionArea area = new PositionArea(0, 0, X_SIZE, Y_SIZE);

        if (map.getSize() != MAP_SIZE) {
            note.addEntry(this,
                          String.format("Game map does contain less or more than %d fields",
                                        MAP_SIZE));
        }

        if (map.getPositions().stream().anyMatch(area::isOutside)) {
            note.addEntry(this, "Game Map contains fields, whose positions are out-of-bounds");
        }
    }
}
