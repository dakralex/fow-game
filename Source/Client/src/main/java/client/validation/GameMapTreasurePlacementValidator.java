package client.validation;

import client.map.GameMap;
import client.map.GameMapNode;

public class GameMapTreasurePlacementValidator implements GameMapValidationRule {

    @Override
    public boolean validate(GameMap map) {
        return map.getMapNodes().stream().noneMatch(GameMapNode::hasTreasure);
    }
}
