package client.validation;

import java.util.List;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.TerrainType;

public class GameMapFortPlacementValidator implements GameMapValidationRule {

    @Override
    public boolean validate(GameMap map) {
        List<GameMapNode> fortNodes = map.getMapNodes().stream()
                .filter(GameMapNode::hasPlayerFort)
                .toList();

        if (fortNodes.size() != 1) {
            return false;
        }

        GameMapNode fortMapNode = fortNodes.getFirst();

        return !fortMapNode.hasOpponentFort()
                && fortMapNode.getTerrainType() == TerrainType.GRASS;
    }
}
