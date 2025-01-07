package client.main.stage;

import java.util.Collection;

import client.main.GameClientState;
import client.map.GameMap;
import client.map.MapDirection;
import client.search.AStarPathFinder;

public class FindTreasure implements Stage {

    @Override
    public Collection<MapDirection> retrieveNextDirections(GameClientState state) {
        GameMap currentMap = state.getMap();
        GameMap playerHalfMap = currentMap.getPlayerHalfMap();

        return AStarPathFinder.getWalkToUnvisitedMapNode(state, playerHalfMap);
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
