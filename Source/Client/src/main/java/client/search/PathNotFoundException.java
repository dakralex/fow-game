package client.search;

import client.map.Position;

public class PathNotFoundException extends RuntimeException {

    private static final String ERROR_MESSAGE_TEMPLATE = "Could not find any path from %s to %s";

    public PathNotFoundException(String message) {
        super(message);
    }

    public PathNotFoundException(Position source, Position destination) {
        super(String.format(ERROR_MESSAGE_TEMPLATE, source, destination));
    }
}
