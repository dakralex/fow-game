package client.validation;

import client.map.GameMap;
import client.map.PositionArea;

public class GameMapPositionsValidator implements GameMapValidationRule {

    private static final int X_SIZE = 10;
    private static final int Y_SIZE = 5;

    @Override
    public void validate(GameMap map, Notification<? super GameMapValidationRule> note) {
        PositionArea area = new PositionArea(0, 0, X_SIZE, Y_SIZE);

        if (map.getPositions().stream().anyMatch(area::isOutside)) {
            note.addEntry(this, "Game Map contains fields, whose positions are out-of-bounds");
        }
    }
}
