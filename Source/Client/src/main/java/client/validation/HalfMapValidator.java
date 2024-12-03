package client.validation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.Position;
import client.map.PositionArea;
import client.map.TerrainType;

public class HalfMapValidator {

    private static final int X_SIZE = 10;
    private static final int Y_SIZE = 5;
    private static final int MAP_SIZE = X_SIZE * Y_SIZE;

    private static final int GRASS_MIN_AMOUNT = 24;
    private static final int MOUNTAIN_MIN_AMOUNT = 5;
    private static final int WATER_MIN_AMOUNT = 7;

    private static final int BORDER_ACCESS_MIN_PERCENTAGE = 51;

    private boolean validateSize(GameMap map) {
        return map.getSize() == MAP_SIZE;
    }

    private boolean validatePositions(GameMap map) {
        PositionArea area = new PositionArea(0, 0, X_SIZE, Y_SIZE);

        return map.getPositions().stream().allMatch(area::isInside);
    }

    private boolean validateTerrainDistribution(GameMap map) {
        List<TerrainType> terrains = map.getMapNodes().stream()
                .map(GameMapNode::getTerrainType)
                .toList();

        int grassCount = Collections.frequency(terrains, TerrainType.GRASS);
        int mountainCount = Collections.frequency(terrains, TerrainType.MOUNTAIN);
        int waterCount = Collections.frequency(terrains, TerrainType.WATER);

        return grassCount >= GRASS_MIN_AMOUNT
                && mountainCount >= MOUNTAIN_MIN_AMOUNT
                && waterCount >= WATER_MIN_AMOUNT;
    }

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

    private boolean validateBorderAccessibility(GameMap map) {
        return Arrays.stream(MapDirection.values())
                .allMatch(direction -> validateEachBorderAccessibility(map, direction));
    }

    private boolean validateFortPlacement(GameMap map) {
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

    private boolean validateTreasurePlacement(GameMap map) {
        return map.getMapNodes().stream().noneMatch(GameMapNode::hasTreasure);
    }

    private void floodFillMap(GameMap map, Position position, Set<Position> visitedNodes) {
        visitedNodes.add(position);

        map.getReachableNeighbors(position).stream()
                .filter(mapNode -> !visitedNodes.contains(mapNode.getPosition()))
                .forEach(mapNode -> floodFillMap(map, mapNode.getPosition(), visitedNodes));
    }

    private boolean validateTerrainReachability(GameMap map) {
        Position fortPosition = map.getMapNodes().stream()
                .filter(GameMapNode::hasPlayerFort)
                .map(GameMapNode::getPosition)
                .findFirst()
                .orElse(Position.originPosition);

        Set<Position> accessiblePositions = map.getMapNodes().stream()
                .filter(GameMapNode::isAccessible)
                .map(GameMapNode::getPosition)
                .collect(Collectors.toSet());

        Set<Position> visitedNodes = HashSet.newHashSet(accessiblePositions.size());

        floodFillMap(map, fortPosition, visitedNodes);

        return visitedNodes.containsAll(accessiblePositions);
    }

    public boolean validate(GameMap map) {
        return validateSize(map)
                && validatePositions(map)
                && validateTerrainDistribution(map)
                && validateBorderAccessibility(map)
                && validateFortPlacement(map)
                && validateTreasurePlacement(map)
                && validateTerrainReachability(map);
    }
}
