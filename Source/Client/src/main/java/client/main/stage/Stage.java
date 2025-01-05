package client.main.stage;

import java.util.Collection;

import client.main.GameClientState;
import client.map.MapDirection;

public interface Stage {

    /**
     * Returns the next sequence of {@link MapDirection} to traverse in the current stage.
     * <p>
     * This method should be executed whenever there are no movement requests left and the
     * current stage wasn't completed yet.
     *
     * @param state the current game state
     * @return list of the next directions to follow
     */
    Collection<MapDirection> retrieveNextDirections(GameClientState state);

}
