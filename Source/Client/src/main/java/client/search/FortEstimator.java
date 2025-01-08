package client.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import client.main.GameClientState;
import client.map.GameMap;
import client.map.GameMapNode;
import client.map.Path;
import client.map.Position;
import client.map.TerrainType;
import client.player.Player;

public class FortEstimator {

    private final GameMap map;
    private final Player player;

    public FortEstimator(GameMap map, Player player) {
        this.map = makeMapDeepClone(map);
        this.player = player;
    }

    public FortEstimator(GameClientState clientState) {
        this(clientState.getMap().getEnemyHalfMap(), clientState.getEnemy().orElseThrow());
    }

    private static GameMap makeMapDeepClone(GameMap map) {
        return new GameMap(map.getMapNodes().stream().map(GameMapNode::new));
    }

    private static void getPossiblePositions(GameMap map, Position source,
                                             Collection<Position> visitedPositions,
                                             Map<Integer, List<Position>> possiblePositions,
                                             int currentTurn) {
        List<Position> positions = possiblePositions.getOrDefault(currentTurn, new ArrayList<>(1));
        positions.add(source);
        visitedPositions.add(source);
        possiblePositions.put(currentTurn, positions);

        GameMapNode sourceMapNode = map.getNodeAt(source).orElseThrow();
        TerrainType sourceTerrainType = sourceMapNode.getTerrainType();

        map.getReachableNeighbors(source).stream()
                .filter(mapNode -> !visitedPositions.contains(mapNode.getPosition()))
                .forEach(mapNode -> {
                    Position currentPosition = mapNode.getPosition();
                    TerrainType currentTerrainType = mapNode.getTerrainType();
                    int travelCost = currentTurn - TerrainType.computeTravelCost(sourceTerrainType,
                                                                                 currentTerrainType);

                    if (travelCost >= 0) {
                        getPossiblePositions(map,
                                             currentPosition,
                                             visitedPositions,
                                             possiblePositions,
                                             travelCost);
                    }
                });
    }

    public Collection<Position> getPossiblePositions(int currentTurn) {
        List<Position> positionHistory = player.getPositionHistory();

        if (currentTurn < 8) {
            return Collections.emptyList();
        }

        GameMap searchMap = makeMapDeepClone(map);

        Position firstPosition = positionHistory.getFirst();

        Path path = new Path(positionHistory);
        int totalTravelCost = path.intoMapDirections(searchMap).size();

        Collection<Position> visitedPositions = new ArrayList<>();
        Map<Integer, List<Position>> possiblePositions = HashMap.newHashMap(41);
        getPossiblePositions(searchMap,
                             firstPosition,
                             visitedPositions,
                             possiblePositions,
                             currentTurn - totalTravelCost);

        return possiblePositions.getOrDefault(0, Collections.emptyList());
    }
}
