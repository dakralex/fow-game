package client.main.stage;

import java.util.Collection;
import java.util.List;

import client.main.GameClientState;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.Position;
import client.search.AStarPathFinder;
import client.search.PathFinder;

public class WalkToTreasure implements Stage {

    private static List<MapDirection> getDirectWalkTo(GameClientState clientState,
                                                      Position destination) {
        PathFinder pathFinder = new AStarPathFinder(clientState.getMap());

        return pathFinder
                .findPath(clientState.getPlayer().getPosition(), destination)
                .intoMapDirections(clientState.getMap());
    }

    private static Position getTreasurePosition(GameClientState state) {
        // TODO Improve error handling here (by transitioning back to searching the treasure?)
        return state.getMapNodePosition(GameMapNode::hasTreasure).orElseThrow();
    }

    @Override
    public Collection<MapDirection> retrieveNextDirections(GameClientState state) {
        Position treasurePosition = getTreasurePosition(state);

        return getDirectWalkTo(state, treasurePosition);
    }
}
