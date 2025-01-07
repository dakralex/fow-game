package client.main.stage;

import java.util.Collection;
import java.util.List;

import client.main.GameClientState;
import client.map.GameMap;
import client.map.MapDirection;
import client.map.Position;
import client.search.AStarPathFinder;
import client.search.PathFinder;

public class FindTreasure implements Stage {

    private static List<MapDirection> getNextWalkToUnvisitedNode(Position source, GameMap map,
                                                                 GameMap haystackMap) {
        // TODO: Improve error handling here
        Position unvisitedPosition = map.getRandomNearbyLootablePosition(source)
                .or(haystackMap::getRandomUnvisitedDeadEndPosition)
                .orElseThrow();
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
