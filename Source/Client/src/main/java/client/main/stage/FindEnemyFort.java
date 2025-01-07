package client.main.stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import client.main.GameClientState;
import client.map.GameMap;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.Position;
import client.map.comparator.TaxicabDistanceComparator;
import client.search.AStarPathFinder;
import client.search.PathFinder;

public class FindEnemyFort implements Stage {

    private static Optional<Position> getRandomNearbyLootableFields(Position source, GameMap map) {
        Comparator<Position> distanceComparator = new TaxicabDistanceComparator(source);
        List<Position> lootableNodes = new ArrayList<>();
        Collection<Position> nearbyLootableNodes = new ArrayList<>();

        do {
            nearbyLootableNodes.clear();
            nearbyLootableNodes.addAll(
                    map.getReachableNeighbors(source)
                            .stream()
                            .filter(GameMapNode::isUnvisited)
                            .filter(GameMapNode::isLootable)
                            .map(GameMapNode::getPosition)
                            .filter(position -> !lootableNodes.contains(position))
                            .sorted(distanceComparator)
                            .toList());
            lootableNodes.addAll(nearbyLootableNodes);
        } while (!nearbyLootableNodes.isEmpty());

        Collections.shuffle(lootableNodes);

        return lootableNodes.stream().findFirst();
    }

    private static List<MapDirection> getNextWalkToUnvisitedNode(Position source, GameMap map,
                                                                 GameMap haystackMap) {
        // TODO: Improve error handling here
        Position unvisitedPosition = getRandomNearbyLootableFields(source, map)
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
