package client.main.stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import client.main.GameClientState;
import client.map.GameMap;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.Position;
import client.map.comparator.FarthestDistanceComparator;
import client.map.comparator.NeighborCountComparator;
import client.search.AStarPathFinder;
import client.search.PathFinder;

public class FindTreasure implements Stage {

    private static Position getRandomUnvisitedMapNode(GameMap map) {
        List<GameMapNode> mapNodes = new ArrayList<>(map.getUnvisitedNodes().stream().toList());
        Collections.shuffle(mapNodes);

        return mapNodes.stream()
                .findFirst()
                .orElseThrow()
                .getPosition();
    }

    private static Position getDeadEndUnvisitedMapNode(GameMap haystackMap) {
        List<GameMapNode> mapNodes = haystackMap.getUnvisitedNodes().stream()
                .sorted(new NeighborCountComparator(haystackMap))
                .toList();

        return mapNodes.stream()
                .findFirst()
                .map(GameMapNode::getPosition)
                .orElseGet(() -> getRandomUnvisitedMapNode(haystackMap));
    }

    private static Optional<Position> getRandomNearbyLootableFields(Position source, GameMap map) {
        Comparator<Position> farthestAwayComparator = new FarthestDistanceComparator(source);
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
                .orElseGet(() -> getDeadEndUnvisitedMapNode(haystackMap));
        PathFinder pathFinder = new AStarPathFinder(map);

        return pathFinder.findPath(source, unvisitedPosition).intoMapDirections(map);
    }

    @Override
    public Collection<MapDirection> retrieveNextDirections(GameClientState state) {
        GameMap currentMap = state.getMap();
        Position currentPosition = state.getPlayerPosition();
        GameMap playerHalfMap = currentMap.getPlayerHalfMap();

        return getNextWalkToUnvisitedNode(currentPosition, currentMap, playerHalfMap);
    }

    @Override
    public boolean hasReachedObjective(GameClientState state) {
        return state.hasFoundTreasure();
    }

    @Override
    public String getStageStartMessage() {
        return "finding the player's treasure";
    }

    @Override
    public String getStageCompletionMessage() {
        return "found the player's treasure";
    }
}
