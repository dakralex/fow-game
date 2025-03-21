package client.validation;

import java.util.Collection;

import client.map.GameMap;
import client.map.GameMapNode;

public class GameMapFortPlacementValidator implements GameMapValidationRule {

    @Override
    public void validate(GameMap map, Notification<? super GameMapValidationRule> note) {
        Collection<GameMapNode> fortNodes = map.getMapNodes(GameMapNode::hasAnyFort);

        if (!fortNodes.stream().allMatch(GameMapNode::isLootable)) {
            note.addEntry(this, "Game map has at least one non-grass field with a fort");
        }

        if (fortNodes.stream().anyMatch(GameMapNode::hasEnemyFort)) {
            note.addEntry(this, "Game map has at least one field with the enemy's fort");
        }

        if (fortNodes.size() != 1) {
            note.addEntry(this, "Game map has no or more than one field with a fort");
        }
    }
}
