package client.map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerHalfMapNode;
import messagesbase.messagesfromserver.FullMap;
import client.util.ANSIColor;

public class GameMap {

    private static final Logger logger = LoggerFactory.getLogger(GameMap.class);

    private final Map<Position, GameMapNode> nodes;

    public GameMap(Collection<GameMapNode> nodes) {
        this.nodes = HashMap.newHashMap(nodes.size());
        this.nodes.putAll(nodes.stream()
                                  .collect(Collectors.toMap(GameMapNode::getPosition,
                                                            Function.identity())));
    }

    public static GameMap fromFullMap(FullMap fullMap) {
        Collection<GameMapNode> fullMapNodes = fullMap.getMapNodes().stream()
                .map(GameMapNode::fromFullMapNode)
                .collect(Collectors.toSet());

        return new GameMap(fullMapNodes);
    }

    public PlayerHalfMap intoPlayerHalfMap(String playerId) {
        Collection<PlayerHalfMapNode> halfMapNodes = nodes.values().stream()
                .map(GameMapNode::intoPlayerHalfMapNode)
                .collect(Collectors.toSet());

        return new PlayerHalfMap(playerId, halfMapNodes);
    }

    public void update(GameMap newMap) {
        newMap.nodes.forEach((position, newMapNode) -> {
            if (nodes.containsKey(position)) {
                GameMapNode mapNode = nodes.get(position);

                mapNode.update(newMapNode);
            } else {
                nodes.put(position, newMapNode);

                logger.debug("Update adds a new GameMapNode at {}", position);
            }
        });
    }

    public Set<Position> getPositions() {
        return Collections.unmodifiableSet(nodes.keySet());
    }

    public Collection<GameMapNode> getMapNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    public int getSize() {
        return nodes.size();
    }

    private GameMapNode getNodeAt(Position position) {
        return nodes.get(position);
    }

    private Stream<GameMapNode> getNeighborsStream(Position position) {
        return Arrays.stream(MapDirection.values())
                .map(position::stepInDirection)
                .map(this::getNodeAt)
                .filter(Objects::nonNull);
    }

    public Set<GameMapNode> getReachableNeighbors(Position position) {
        return getNeighborsStream(position)
                .filter(GameMapNode::isAccessible)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("   | ");

        PositionArea area = getArea();
        IntStream.range(0, area.width())
                .forEach(index -> stringBuilder.append(String.format("%d", index % 10)));

        stringBuilder.append("\n");

        for (int y = 0; y < area.height(); ++y) {
            stringBuilder.append(String.format("%2d | ", y));
            for (int x = 0; x < area.width(); ++x) {
                Position currentPosition = new Position(x, y);

                if (!nodes.containsKey(currentPosition)) {
                    stringBuilder.append(ANSIColor.format("a",
                                                          ANSIColor.BRIGHT_BLACK,
                                                          ANSIColor.BRIGHT_BLACK));
                } else {
                    GameMapNode currentMapNode = nodes.get(currentPosition);
                    stringBuilder.append(String.format("%s", currentMapNode));
                }
            }
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }
}
