package client.validation;

import java.util.List;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.TerrainType;

public class GameMapFortPlacementValidator implements GameMapValidationRule {

    @Override
    public void validate(GameMap map, Notification<? super GameMapValidationRule> note) {
        List<GameMapNode> fortNodes = map.getMapNodes().stream()
                .filter(GameMapNode::hasAnyFort)
                .toList();

        if (fortNodes.stream().anyMatch(GameMapNode::hasEnemyFort)) {
            note.addEntry(this, "Game map has at least one field with the enemy's fort");
        }

        if (fortNodes.size() != 1) {
            note.addEntry(this, "Game map has no or more than one field with a fort");
        }

        GameMapNode fortMapNode = fortNodes.getFirst();

        if (fortMapNode.getTerrainType() != TerrainType.GRASS) {
            note.addEntry(this, "Game map has a non-grass field with the fort");
        }
    }
}
