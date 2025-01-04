package client.validation;

import java.util.Set;
import java.util.stream.Collectors;

import client.map.GameMap;
import client.map.Position;
import client.map.PositionArea;

public class GameMapDimensionValidator implements GameMapValidationRule {

    private static final int X_SIZE = 10;
    private static final int Y_SIZE = 5;

    @Override
    public void validate(GameMap map, Notification<? super GameMapValidationRule> note) {
        PositionArea actualArea = map.getArea();
        PositionArea expectedArea = new PositionArea(0, 0, X_SIZE, Y_SIZE);
        Set<Position> areaPositions = expectedArea.intoPositionStream()
                .collect(Collectors.toUnmodifiableSet());

        if (!actualArea.equals(expectedArea)) {
            note.addEntry(this, "Game Map does not have the expected dimensions");
        }

        if (!map.getPositions().equals(areaPositions)) {
            note.addEntry(this, "Game Map contains fields with unexpected positions");
        }
    }
}
