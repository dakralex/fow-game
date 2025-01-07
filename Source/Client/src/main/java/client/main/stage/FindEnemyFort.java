package client.main.stage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import client.main.GameClientState;
import client.map.GameMap;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.Position;
import client.search.AStarPathFinder;
import client.search.PathFinder;

public class FindEnemyFort implements Stage {

    private static List<MapDirection> getNextWalkToUnvisitedNode(Position source, GameMap map,
                                                                 GameMap haystackMap) {
        // TODO: Improve error handling here
        Position unvisitedPosition = map.getRandomNearbyLootablePosition(source)
                .or(haystackMap::getRandomUnvisitedDeadEndPosition)
                .orElseThrow();
        PathFinder pathFinder = new AStarPathFinder(map);

        return pathFinder.findPath(source, unvisitedPosition).intoMapDirections(map);
    }

    private static Optional<Position> getWaterProtectedFortPosition(GameMap map,
                                                                    GameMap haystackMap) {
        return haystackMap.getMapNodes().stream()
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
        Position currentPosition = state.getPlayerPosition();
        GameMap enemyHalfMap = currentMap.getEnemyHalfMap();

        return getWaterProtectedFortPosition(currentMap, enemyHalfMap)
                .map(possiblePosition -> AStarPathFinder.getDirectWalkTo(state, possiblePosition))
                .orElseGet(() -> getNextWalkToUnvisitedNode(currentPosition,
                                                            currentMap,
                                                            enemyHalfMap));
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
