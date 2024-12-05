package client.map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import client.util.ANSIColor;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerHalfMapNode;
import messagesbase.messagesfromserver.FullMap;

public class GameMap {

    public static final Collector<GameMapNode, ?, Map<Position, GameMapNode>> mapCollector =
            Collectors.toMap(GameMapNode::getPosition, Function.identity());

    private static final Logger logger = LoggerFactory.getLogger(GameMap.class);
    private static final int MAP_FULL_SIZE = 100;

    private final Map<Position, GameMapNode> nodes;

    public GameMap(Collection<GameMapNode> nodes) {
        this.nodes = HashMap.newHashMap(nodes.size());
        this.nodes.putAll(nodes.stream().collect(mapCollector));
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

    public void update(GameMap newMap, Position viewpointPosition) {
        Collection<Position> visiblePositions = getPositionsInSight(viewpointPosition);

        newMap.nodes.forEach((position, newMapNode) -> {
            if (nodes.containsKey(position)) {
                GameMapNode mapNode = nodes.get(position);
                boolean isNodeInSight = visiblePositions.contains(position);

                mapNode.update(newMapNode, isNodeInSight);
            } else {
                // TODO: Find a better way to initially set all map nodes to unknown
                newMapNode.resetVisibility();
                nodes.put(position, newMapNode);
            }
        });
    }

    public Set<Position> getPositions() {
        return Collections.unmodifiableSet(nodes.keySet());
    }

    private Collection<Position> getPositions(Predicate<Position> predicate) {
        return getPositions().stream().filter(predicate).toList();
    }

    private Collection<Position> getPositionsInSight(Position cameraPosition) {
        int cameraViewRadius = getNodeAt(cameraPosition)
                .map(GameMapNode::getTerrainType)
                .map(TerrainType::getViewRadius)
                .orElse(-1);

        Collection<Position> visiblePositions = getPositions(position -> {
            int distance = cameraPosition.chebyshevDistanceTo(position);

            return distance <= cameraViewRadius;
        });

        return new ArrayList<>(visiblePositions);
    }

    public Collection<GameMapNode> getMapNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    private Collection<GameMapNode> getMapNodes(Predicate<GameMapNode> predicate) {
        return getMapNodes().stream().filter(predicate).toList();
    }

    private PositionArea getArea() {
        Position minPosition = getPositions().stream().min(Position::compareTo).orElseThrow();
        Position maxPosition = getPositions().stream().max(Position::compareTo).orElseThrow();

        return new PositionArea(minPosition, maxPosition);
    }

    private Collection<GameMapNode> getHalfMapNodes(Position position) {
        Collection<GameMapNode> mapNodes = new ArrayList<>(getSize() / 2);
        PositionArea mapArea = getArea();
        Position middlePoint = mapArea.middlePoint();

        Predicate<GameMapNode> predicate;

        // TODO: Make this calculation easier to read, I don't have time for that now
        // The GameMap can be either square (10 x 10) or wide (20 x 5), when both clients have
        // sent their half maps, so we have consider either case for getting the half map
        if (mapArea.isLandscape()) {
            if (position.x() < middlePoint.x()) {
                predicate = mapNode -> mapNode.getPosition().x() < middlePoint.x();
            } else {
                predicate = mapNode -> mapNode.getPosition().x() >= middlePoint.x();
            }
        } else {
            if (position.y() < middlePoint.y()) {
                predicate = mapNode -> mapNode.getPosition().y() < middlePoint.y();
            } else {
                predicate = mapNode -> mapNode.getPosition().y() >= middlePoint.y();
            }
        }

        mapNodes.addAll(getMapNodes(predicate));

        return mapNodes;
    }

    private Position getPlayerFortPosition() {
        return getMapNodes().stream()
                .filter(GameMapNode::hasPlayerFort)
                .findFirst()
                .map(GameMapNode::getPosition)
                .orElseThrow();
    }

    public Collection<GameMapNode> getPlayerMapNodes() {
        return getHalfMapNodes(getPlayerFortPosition());
    }

    public Collection<GameMapNode> getOpponentMapNodes() {
        Position playerFortPosition = getPlayerFortPosition();

        // TODO: Refactor/Improve this reflection calculation
        PositionArea mapArea = getArea();
        Position middlePoint = mapArea.middlePoint();
        Position reflectivePosition;

        if (mapArea.isLandscape()) {
            int xMiddleDistance = middlePoint.x() - playerFortPosition.x();
            int xReflectivePosition = playerFortPosition.x() + 2 * xMiddleDistance;
            reflectivePosition = new Position(xReflectivePosition, playerFortPosition.y());
        } else {
            int yMiddleDistance = middlePoint.y() - playerFortPosition.y();
            int yReflectivePosition = playerFortPosition.y() + 2 * yMiddleDistance;
            reflectivePosition = new Position(playerFortPosition.x(), yReflectivePosition);
        }

        return getHalfMapNodes(reflectivePosition);
    }

    public int getSize() {
        return nodes.size();
    }

    public boolean isFullMap() {
        return getSize() == MAP_FULL_SIZE;
    }

    public Optional<GameMapNode> getNodeAt(Position position) {
        return Optional.ofNullable(nodes.get(position));
    }

    private Stream<GameMapNode> getNeighborsStream(Position position) {
        return Arrays.stream(MapDirection.values())
                .map(position::stepInDirection)
                .map(this::getNodeAt)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public Set<GameMapNode> getAllNeighbors(Position position) {
        return getNeighborsStream(position)
                .collect(Collectors.toUnmodifiableSet());
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
