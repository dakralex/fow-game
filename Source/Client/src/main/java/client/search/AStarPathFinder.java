package client.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.Path;
import client.map.Position;
import client.map.TerrainType;

public class AStarPathFinder implements PathFinder {

    private static final int DIRECTION_COUNT = MapDirection.values().length;

    private final Map<Position, GameMapNode> mapNodes;

    public AStarPathFinder(Collection<GameMapNode> mapNodes) {
        this.mapNodes = HashMap.newHashMap(mapNodes.size());
        this.mapNodes.putAll(mapNodes.stream().collect(GameMap.mapCollector));
    }

    private static Comparator<GameMapNode> getCostComparator(Map<Position, Integer> costToEndNode) {
        return (a, b) -> {
            int aCostToEnd = costToEndNode.getOrDefault(a.getPosition(), Integer.MAX_VALUE);
            int bCostToEnd = costToEndNode.getOrDefault(b.getPosition(), Integer.MAX_VALUE);

            return aCostToEnd - bCostToEnd;
        };
    }

    private static int computeRemainingCost(Position source, Position destination) {
        return source.taxicabDistanceTo(destination);
    }

    private Optional<GameMapNode> getNodeAt(Position position) {
        return Optional.ofNullable(mapNodes.get(position));
    }

    private Stream<GameMapNode> getNeighborsStream(Position position) {
        return Arrays.stream(MapDirection.values())
                .map(position::stepInDirection)
                .map(this::getNodeAt)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private List<GameMapNode> getReachableNeighbors(Position position) {
        return getNeighborsStream(position)
                .filter(GameMapNode::isAccessible)
                .toList();
    }

    private Set<Position> getPositions() {
        return Collections.unmodifiableSet(mapNodes.keySet());
    }

    private Collection<Position> getPositions(Predicate<Position> predicate) {
        return getPositions().stream().filter(predicate).toList();
    }

    private Collection<Position> getPositionsInSight(Position cameraPosition) {
        // TODO: Reduce code duplication from GameMap
        int cameraViewRadius = getNodeAt(cameraPosition)
                .map(GameMapNode::getTerrainType)
                .map(TerrainType::getViewRadius)
                .orElse(-1);

        Collection<Position> visiblePositions = getPositions(position -> {
            int distance = cameraPosition.chebyshevDistanceTo(position);

            return distance <= cameraViewRadius;
        });

        return new ArrayList<>(visiblePositions);
    }

    private int computeTravelCost(GameMapNode from, GameMapNode to) {
        int totalCost = 3 * TerrainType.computeTravelCost(from.getTerrainType(),
                                                          to.getTerrainType());

        Collection<Position> visiblePositions = getPositionsInSight(to.getPosition());
        long explorationValue = visiblePositions.stream()
                .map(this::getNodeAt)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(GameMapNode::isUnvisited)
                .count();

        totalCost -= (int) explorationValue;

        return Math.min(0, totalCost);
    }

    private static Path reconstructPath(Position source, Position destination,
                                        Map<Position, Position> cameFrom) {
        SequencedCollection<Position> pathNodes = new ArrayList<>(cameFrom.size() + 1);

        Position currentPosition = destination;

        while (currentPosition != source) {
            pathNodes.add(currentPosition);
            currentPosition = cameFrom.get(currentPosition);
        }

        return new Path(pathNodes.reversed());
    }

    @Override
    public Path findPath(Position source, Position destination) {
        Map<Position, Integer> costToStartNode = HashMap.newHashMap(mapNodes.size());
        Map<Position, Integer> costToEndNode = HashMap.newHashMap(mapNodes.size());

        Queue<GameMapNode> openSet = new PriorityQueue<>(DIRECTION_COUNT + 1,
                                                         getCostComparator(costToEndNode));
        openSet.add(getNodeAt(source).orElseThrow());

        Map<Position, Position> cameFrom = new HashMap<>();
        cameFrom.put(source, source);

        costToStartNode.put(source, 0);
        costToEndNode.put(source, computeRemainingCost(source, destination));

        while (!openSet.isEmpty()) {
            GameMapNode currentNode = openSet.remove();
            Position currentPosition = currentNode.getPosition();

            if (currentPosition.equals(destination)) {
                return reconstructPath(source, destination, cameFrom);
            }

            List<GameMapNode> neighborNodes = getReachableNeighbors(currentPosition);

            for (GameMapNode neighborNode : neighborNodes) {
                Position neighborPosition = neighborNode.getPosition();

                int moveCost = computeTravelCost(currentNode, neighborNode);
                int travelCost = costToStartNode.get(currentPosition) + moveCost;

                int minTravelCost = costToStartNode.getOrDefault(neighborPosition,
                                                                 Integer.MAX_VALUE);

                if (travelCost < minTravelCost) {
                    int remainingCost = computeRemainingCost(neighborPosition, destination);
                    int totalTravelCost = travelCost + remainingCost;

                    costToStartNode.put(neighborPosition, travelCost);
                    costToEndNode.put(neighborPosition, totalTravelCost);

                    cameFrom.put(neighborPosition, currentPosition);
                    openSet.add(neighborNode);
                }
            }
        }

        String errorMessage =
                String.format("Could not find any path from %s to %s", source, destination);

        throw new IllegalStateException(errorMessage);
    }
}
