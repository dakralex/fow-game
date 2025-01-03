package client.validation;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.PositionArea;

public class GameMapBorderAccessibilityValidator implements GameMapValidationRule {

    private static final int BORDER_ACCESS_MIN_PERCENTAGE = 51;

    private static Predicate<GameMapNode> getBorderPredicate(GameMap map, MapDirection direction) {
        PositionArea mapArea = map.getArea();

        return switch (direction) {
            case EAST -> mapNode -> mapArea.isOnEastBorder(mapNode.getPosition());
            case NORTH -> mapNode -> mapArea.isOnNorthBorder(mapNode.getPosition());
            case SOUTH -> mapNode -> mapArea.isOnSouthBorder(mapNode.getPosition());
            case WEST -> mapNode -> mapArea.isOnWestBorder(mapNode.getPosition());
        };
    }

    private static boolean isBorderAccessible(GameMap map, MapDirection direction) {
        Predicate<GameMapNode> isOnBorder = getBorderPredicate(map, direction);
        List<GameMapNode> borderNodes = map.getMapNodes().stream().filter(isOnBorder).toList();

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
