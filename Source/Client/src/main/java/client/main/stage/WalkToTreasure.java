package client.main.stage;

import java.util.Collection;

import client.main.GameClientState;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.Position;
import client.search.AStarPathFinder;

public class WalkToTreasure implements Stage {

    private static UnexpectedStageException provideInvalidStateException() {
        return new UnexpectedStageException("Cannot walk to player's treasure, if it isn't known yet.");
    }

    @Override
    public Collection<MapDirection> retrieveNextDirections(GameClientState state) {
        Position treasurePosition = state.getMapNodePosition(GameMapNode::hasTreasure)
                .orElseThrow(WalkToTreasure::provideInvalidStateException);

        return AStarPathFinder.getDirectWalkTo(state, treasurePosition);
    }

    @Override
    public boolean hasReachedObjective(GameClientState state) {
        return state.hasCollectedTreasure();
    }

    @Override
    public String getStageStartMessage() {
        return "going to the player's treasure";
    }

    @Override
    public String getStageCompletionMessage() {
        return "collected the player's treasure";
    }
}
