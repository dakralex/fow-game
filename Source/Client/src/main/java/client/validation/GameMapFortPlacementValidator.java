package client.validation;

import java.util.List;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.TerrainType;

public class GameMapFortPlacementValidator implements GameMapValidationRule {

    @Override
    public void validate(GameMap map, Notification<GameMapValidationRule> note) {
        List<GameMapNode> fortNodes = map.getMapNodes().stream()
                .filter(GameMapNode::hasPlayerFort)
                .toList();

        if (fortNodes.size() != 1) {
            note.addEntry(this, "Game map has no or more than one field with a fort");
        }

        GameMapNode fortMapNode = fortNodes.getFirst();

        if (fortMapNode.getTerrainType() != TerrainType.GRASS) {
            note.addEntry(this, "Game map has a non-grass field with the fort");
        }

        if (fortMapNode.hasOpponentFort()) {
            note.addEntry(this, "Game map has a field with the opponent's fort");
        }
    }
}
