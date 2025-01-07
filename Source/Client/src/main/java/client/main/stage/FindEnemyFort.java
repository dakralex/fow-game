package client.main.stage;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import client.main.GameClientState;
import client.map.GameMap;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.Position;
import client.search.AStarPathFinder;

public class FindEnemyFort implements Stage {

    private static Optional<Position> findFortStructurePosition(GameMap map) {
        return map.getMapNodes().stream()
                .filter(GameMapNode::isLootable)
                .filter(GameMapNode::isUnvisited)
                .map(GameMapNode::getPosition)
                .filter(mapNodePosition -> {
                    Set<GameMapNode> neighborNodes = map.getAllNeighbors(mapNodePosition);

                    return neighborNodes.stream()
                            .filter(neighborNode -> !neighborNode.isAccessible())
                            .count() > 2L;
                })
                .findFirst();
    }

    @Override
    public Collection<MapDirection> retrieveNextDirections(GameClientState state) {
        GameMap currentMap = state.getMap();
        GameMap enemyHalfMap = currentMap.getEnemyHalfMap();

        return findFortStructurePosition(enemyHalfMap)
                .map(possiblePosition -> AStarPathFinder.getDirectWalkTo(state, possiblePosition))
                .orElseGet(() -> AStarPathFinder.getWalkToUnvisitedMapNode(state, enemyHalfMap));
    }

    @Override
    public boolean hasReachedObjective(GameClientState state) {
        return state.hasFoundEnemyFort();
    }

    @Override
    public String getStageStartMessage() {
        return "finding the enemy's fort";
    }

    @Override
    public String getStageCompletionMessage() {
        return "found the enemy's fort";
    }
}
