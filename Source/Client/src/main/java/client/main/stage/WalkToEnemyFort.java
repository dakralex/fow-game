package client.main.stage;

import java.util.Collection;

import client.main.GameClientState;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.Position;
import client.search.AStarPathFinder;

public class WalkToEnemyFort implements Stage {

    @Override
    public Collection<MapDirection> retrieveNextDirections(GameClientState state) {
        // TODO: Improve error handling here (by transitioning back to searching enemy fort?)
        Position fortPosition = state.getMapNodePosition(GameMapNode::hasEnemyFort).orElseThrow();

        return AStarPathFinder.getDirectWalkTo(state, fortPosition);
    }

    @Override
    public boolean hasReachedObjective(GameClientState state) {
        return state.hasClientWon();
    }

    @Override
    public String getStageStartMessage() {
        return "going to the enemy's fort";
    }

    @Override
    public String getStageCompletionMessage() {
        return "client has won";
    }
}
