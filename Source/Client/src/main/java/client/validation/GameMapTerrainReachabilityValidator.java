package client.validation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.Position;

public class GameMapTerrainReachabilityValidator implements GameMapValidationRule {

    private static void floodFillMap(GameMap map, Position position, Set<Position> visitedNodes) {
        visitedNodes.add(position);

        map.getReachableNeighbors(position).stream()
                .filter(mapNode -> !visitedNodes.contains(mapNode.getPosition()))
                .forEach(mapNode -> floodFillMap(map, mapNode.getPosition(), visitedNodes));
    }

    @Override
    public void validate(GameMap map, Notification<? super GameMapValidationRule> note) {
        Position fortPosition = map.getPlayerFortPosition().orElse(Position.originPosition);

        Collection<Position> accessiblePositions = map.getPositionsByMapNode(GameMapNode::isAccessible);

        Set<Position> visitedNodes = HashSet.newHashSet(accessiblePositions.size());

        floodFillMap(map, fortPosition, visitedNodes);

        if (!visitedNodes.containsAll(accessiblePositions)) {
            note.addEntry(this,
                          "Game map does contain accessible fields, that cannot be reached (i.e. islands)");
        }
    }
}
