package client.validation;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.PositionArea;

public class GameMapBorderAccessibilityValidator implements GameMapValidationRule {

    private static final int X_SIZE = 10;
    private static final int Y_SIZE = 5;

    private static final int BORDER_ACCESS_MIN_PERCENTAGE = 51;

    private boolean validateEachBorderAccessibility(GameMap map, MapDirection direction) {
        PositionArea mapArea = new PositionArea(0, 0, X_SIZE, Y_SIZE);
        Predicate<GameMapNode> isOnBorder = switch (direction) {
            case EAST -> mapNode -> mapArea.isOnEastBorder(mapNode.getPosition());
            case NORTH -> mapNode -> mapArea.isOnNorthBorder(mapNode.getPosition());
            case SOUTH -> mapNode -> mapArea.isOnSouthBorder(mapNode.getPosition());
            case WEST -> mapNode -> mapArea.isOnWestBorder(mapNode.getPosition());
        };
        List<GameMapNode> borderNodes = map.getMapNodes().stream().filter(isOnBorder).toList();

        long nodeCount = borderNodes.size();
        long minAccessibleNodeCount = Math.ceilDiv(BORDER_ACCESS_MIN_PERCENTAGE * nodeCount, 100);
        long accessibleNodeCount = borderNodes.stream().filter(GameMapNode::isAccessible).count();

        return accessibleNodeCount >= minAccessibleNodeCount;
    }

    @Override
    public void validate(GameMap map, Notification<? super GameMapValidationRule> note) {
        boolean allBordersAccessibleEnough = Arrays.stream(MapDirection.values())
                .allMatch(direction -> validateEachBorderAccessibility(map, direction));

        if (!allBordersAccessibleEnough) {
            note.addEntry(this,
                          String.format("Game map has a border, where less than %d %% is accessible",
                                        BORDER_ACCESS_MIN_PERCENTAGE));
        }
    }
}
