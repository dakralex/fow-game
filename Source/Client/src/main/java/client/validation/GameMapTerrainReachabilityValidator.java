package client.validation;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.Position;

public class GameMapTerrainReachabilityValidator implements GameMapValidationRule {

    private void floodFillMap(GameMap map, Position position, Set<Position> visitedNodes) {
        visitedNodes.add(position);

        map.getReachableNeighbors(position).stream()
                .filter(mapNode -> !visitedNodes.contains(mapNode.getPosition()))
                .forEach(mapNode -> floodFillMap(map, mapNode.getPosition(), visitedNodes));
    }

    @Override
    public boolean validate(GameMap map) {
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
}
