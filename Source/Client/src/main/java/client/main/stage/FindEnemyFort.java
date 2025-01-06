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
import client.map.comparator.NeighborCountComparator;
import client.search.AStarPathFinder;
import client.search.PathFinder;

public class FindEnemyFort implements Stage {

    private static Position getRandomUnvisitedMapNode(GameMap map) {
        List<GameMapNode> mapNodes = new ArrayList<>(map.getUnvisitedNodes().stream().toList());
        Collections.shuffle(mapNodes);

        return mapNodes.stream()
                .findFirst()
                .orElseThrow()
                .getPosition();
    }

    private static Position getDeadEndUnvisitedMapNode(GameMap map, GameMap haystackMap) {
        List<GameMapNode> mapNodes = haystackMap.getUnvisitedNodes().stream()
                .sorted(new NeighborCountComparator(map))
                .toList();

        return mapNodes.stream()
                .findFirst()
                .map(GameMapNode::getPosition)
                .orElseGet(() -> getRandomUnvisitedMapNode(haystackMap));
    }

    private static Comparator<Position> getFarthestAwayComparator(Position source) {
        return (a, b) -> {
            int aDistance = source.taxicabDistanceTo(a);
            int bDistance = source.taxicabDistanceTo(b);

            return aDistance - bDistance;
        };
    }

    private static Optional<Position> getRandomNearbyLootableFields(Position source, GameMap map) {
        Comparator<Position> farthestAwayComparator = getFarthestAwayComparator(source);
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
                            .sorted(farthestAwayComparator)
                            .toList());
            lootableNodes.addAll(nearbyLootableNodes);
        } while (!nearbyLootableNodes.isEmpty());

        Collections.shuffle(lootableNodes);

        return lootableNodes.stream().findFirst();
    }

    private static List<MapDirection> getNextWalkToUnvisitedNode(Position source, GameMap map,
                                                                 GameMap haystackMap) {
        Position unvisitedPosition = getRandomNearbyLootableFields(source, map)
                .orElseGet(() -> getDeadEndUnvisitedMapNode(map, haystackMap));
        PathFinder pathFinder = new AStarPathFinder(map);

        return pathFinder.findPath(source, unvisitedPosition).intoMapDirections(map);
    }

    private static List<MapDirection> getDirectWalkTo(GameClientState clientState,
                                                      Position destination) {
        PathFinder pathFinder = new AStarPathFinder(clientState.getMap());

        return pathFinder
                .findPath(clientState.getPlayer().getPosition(), destination)
                .intoMapDirections(clientState.getMap());
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
        Position currentPosition = state.getPlayer().getPosition();
        GameMap enemyHalfMap = currentMap.getEnemyHalfMap();

        return getWaterProtectedFortPosition(currentMap, enemyHalfMap)
                .map(possiblePosition -> getDirectWalkTo(state, possiblePosition))
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
