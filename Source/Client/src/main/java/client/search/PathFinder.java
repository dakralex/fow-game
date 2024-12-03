package client.search;

import client.map.Path;
import client.map.Position;

public interface PathFinder {

    Path findPath(Position source, Position destination);
}
