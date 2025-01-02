package client.validation;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.Position;

public class HalfMapValidator {

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
        GameMapSizeValidator mapSizeValidator = new GameMapSizeValidator();
        GameMapPositionsValidator mapPositionsValidator = new GameMapPositionsValidator();
        GameMapTerrainDistributionValidator mapTerrainDistributionValidator = new GameMapTerrainDistributionValidator();
        GameMapBorderAccessibilityValidator mapBorderAccessibilityValidator = new GameMapBorderAccessibilityValidator();
        GameMapFortPlacementValidator mapFortPlacementValidator = new GameMapFortPlacementValidator();

        return mapSizeValidator.validate(map)
                && mapPositionsValidator.validate(map)
                && mapTerrainDistributionValidator.validate(map)
                && mapBorderAccessibilityValidator.validate(map)
                && mapFortPlacementValidator.validate(map)
                && validateTreasurePlacement(map)
                && validateTerrainReachability(map);
    }
}
