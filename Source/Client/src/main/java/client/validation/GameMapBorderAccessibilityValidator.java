package client.validation;

import java.util.Arrays;
import java.util.Collection;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.MapDirection;

public class GameMapBorderAccessibilityValidator implements GameMapValidationRule {

    private static final int BORDER_ACCESS_MIN_PERCENTAGE = 51;

    private static boolean isBorderAccessible(GameMap map, MapDirection direction) {
        Collection<GameMapNode> borderNodes = map.getBorderNodes(direction);

        int nodeCount = borderNodes.size();
        int minAccessibleNodeCount = Math.ceilDiv(BORDER_ACCESS_MIN_PERCENTAGE * nodeCount, 100);
        long accessibleNodeCount = borderNodes.stream().filter(GameMapNode::isAccessible).count();

        return accessibleNodeCount >= minAccessibleNodeCount;
    }

    @Override
    public void validate(GameMap map, Notification<? super GameMapValidationRule> note) {
        boolean areBordersInaccessible = !Arrays.stream(MapDirection.values())
                .allMatch(direction -> isBorderAccessible(map, direction));

        if (areBordersInaccessible) {
            note.addEntry(this,
                          String.format("Game map has a border, where less than %d %% is accessible",
                                        BORDER_ACCESS_MIN_PERCENTAGE));
        }
    }
}
