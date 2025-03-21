package client.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.SequencedCollection;

import client.main.GameClientState;
import client.map.GameMap;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.Path;
import client.map.Position;
import client.map.TerrainType;

public class AStarPathFinder implements PathFinder {

    private static final int DIRECTION_COUNT = MapDirection.values().length;

    private final GameMap map;

    public AStarPathFinder(GameMap map) {
        this.map = new GameMap(map.getMapNodes());
    }

    public static List<MapDirection> getDirectWalkTo(GameClientState clientState,
                                                     Position destination) {
        GameMap map = clientState.getMap();
        PathFinder pathFinder = new AStarPathFinder(map);
        Path path = pathFinder.findPath(clientState.getPlayerPosition(), destination);

        return path.intoMapDirections(map);
    }

    public static List<MapDirection> getWalkToUnvisitedMapNode(GameClientState clientState,
                                                               GameMap haystackMap) {
        GameMap map = clientState.getMap();
        Position source = clientState.getPlayerPosition();
        Position destination = map.getRandomNearbyLootablePosition(source)
                .or(haystackMap::getRandomUnvisitedDeadEndPosition)
                .orElseThrow();
        PathFinder pathFinder = new AStarPathFinder(map);
        Path path = pathFinder.findPath(source, destination);

        return path.intoMapDirections(map);
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

    private int computeTravelCost(GameMapNode from, GameMapNode to) {
        int totalCost = 3 * TerrainType.computeTravelCost(from.getTerrainType(),
                                                          to.getTerrainType());

        Collection<Position> visiblePositions = map.getPositionsInSight(to.getPosition());
        long explorationValue = visiblePositions.stream()
                .map(map::getNodeAt)
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
        Map<Position, Integer> costToStartNode = HashMap.newHashMap(map.getSize());
        Map<Position, Integer> costToEndNode = HashMap.newHashMap(map.getSize());

        Queue<GameMapNode> openSet = new PriorityQueue<>(DIRECTION_COUNT + 1,
                                                         getCostComparator(costToEndNode));
        openSet.add(map.getNodeAt(source).orElseThrow());

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

            Collection<GameMapNode> neighborNodes = map.getReachableNeighbors(currentPosition);

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

        throw new PathNotFoundException(source, destination);
    }
}
