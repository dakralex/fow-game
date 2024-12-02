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
import java.util.stream.Stream;

import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerHalfMapNode;
import messagesbase.messagesfromserver.EPlayerPositionState;
import messagesbase.messagesfromserver.FullMap;
import messagesbase.messagesfromserver.FullMapNode;

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

    private static boolean isPlayerOnFullMapNode(FullMapNode fullMapNode) {
        return fullMapNode.getPlayerPositionState() == EPlayerPositionState.BothPlayerPosition ||
                fullMapNode.getPlayerPositionState() == EPlayerPositionState.MyPlayerPosition;
    }

    public static Position getPlayerPosition(FullMap fullMap) {
        return fullMap.getMapNodes().stream()
                .filter(GameMap::isPlayerOnFullMapNode)
                .findFirst()
                .map(Position::fromFullMapNode)
                .orElse(Position.originPosition);
    }

    private static boolean isOpponentOnFullMapNode(FullMapNode fullMapNode) {
        return fullMapNode.getPlayerPositionState() == EPlayerPositionState.BothPlayerPosition ||
                fullMapNode.getPlayerPositionState() == EPlayerPositionState.EnemyPlayerPosition;
    }

    public static Position getOpponentPosition(FullMap fullMap) {
        return fullMap.getMapNodes().stream()
                .filter(GameMap::isOpponentOnFullMapNode)
                .findFirst()
                .map(Position::fromFullMapNode)
                .orElse(Position.originPosition);
    }

    public void update(GameMap newMap) {
        newMap.nodes.forEach((position, newMapNode) -> {
            if (!nodes.containsKey(position)) {
                logger.debug("Update adds a new GameMapNode at {}", position);
            }

            GameMapNode mapNode = nodes.getOrDefault(position, newMapNode);

            mapNode.update(newMapNode);
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

    public PlayerHalfMap intoPlayerHalfMap(String playerId) {
        Collection<PlayerHalfMapNode> halfMapNodes = nodes.values().stream()
                .map(GameMapNode::intoPlayerHalfMapNode)
                .collect(Collectors.toSet());

        return new PlayerHalfMap(playerId, halfMapNodes);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("  | 0 1 2 3 4 5 6 7 8 9\n");

        for (int y = 0; y < 5; ++y) {
            stringBuilder.append(String.format("%s | ", y));
            for (int x = 0; x < 10; ++x) {
                Position currentPosition = new Position(x, y);

                if (!nodes.containsKey(currentPosition)) {
                    stringBuilder.append("a ");
                } else {
                    GameMapNode currentMapNode = nodes.get(currentPosition);
                    stringBuilder.append(String.format("%s ", currentMapNode));
                }
            }
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }
}
