package client.search;

import client.map.Path;
import client.map.Position;

public interface PathFinder {

    /**
     * Finds a path between a {@code source} and {@code destination}.
     * <p>
     * This method is useful for any search where a {@code destination} is known in advance.
     *
     * @param source the originating source from which the path search starts
     * @param destination the destination to which the path should go to
     * @return the found path
     * @throws PathNotFoundException if no path could be found
     */
    Path findPath(Position source, Position destination);
}
