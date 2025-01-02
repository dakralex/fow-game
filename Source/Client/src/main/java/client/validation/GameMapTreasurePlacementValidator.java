package client.validation;

import client.map.GameMap;
import client.map.GameMapNode;

public class GameMapTreasurePlacementValidator implements GameMapValidationRule {

    @Override
    public void validate(GameMap map, Notification<? super GameMapValidationRule> note) {
        if (map.getMapNodes().stream().anyMatch(GameMapNode::hasTreasure)) {
            note.addEntry(this, "Game map has at least one field with a treasure");
        }
    }
}
