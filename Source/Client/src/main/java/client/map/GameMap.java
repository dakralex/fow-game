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

    public GameMap(Map<Position, GameMapNode> nodes) {
        this.nodes = HashMap.newHashMap(nodes.size());
        this.nodes.putAll(nodes);
    }

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
                // Update the GameMapNode(s) if already present...
                GameMapNode mapNode = nodes.get(position);
                boolean isNodeInSight = visiblePositions.contains(position);

                mapNode.update(newMapNode, isNodeInSight);
            } else {
                // ...but reset its intelligence if it wasn't present yet.
                //    This is because the server provides a weaker version of intelligence states
                //    for GameMapNodes and therefore we cannot rely on them initially.
                newMapNode.resetIntelligence();
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

    public Collection<GameMapNode> getMapNodes(Predicate<GameMapNode> predicate) {
        return getMapNodes().stream().filter(predicate).toList();
    }

    public PositionArea getArea() {
        Position minPosition = getPositions().stream().min(Position::compareTo).orElseThrow();
        Position maxPosition = getPositions().stream().max(Position::compareTo).orElseThrow();

        return new PositionArea(minPosition, maxPosition);
    }

    private Predicate<GameMapNode> getBorderPredicate(MapDirection direction) {
        return mapNode -> getArea().getBorderPredicate(direction).test(mapNode.getPosition());
    }

    public Collection<GameMapNode> getBorderNodes(MapDirection direction) {
        Predicate<GameMapNode> isOnBorder = getBorderPredicate(direction);

        return getMapNodes(isOnBorder);
    }

    private Predicate<GameMapNode> getHalfMapBinaryRelation(Position position) {
        Predicate<GameMapNode> predicate;
        PositionArea mapArea = getArea();
        Position middlePoint = mapArea.middlePoint();

        // The GameMap can be either square (10 x 10) or wide (20 x 5), when both clients have
        // sent their half maps, so we have consider either case for getting the half map
        if (mapArea.isLandscape()) {
            if (position.isHorizontallyLessThan(middlePoint)) {
                predicate = mapNode -> mapNode.isHorizontallyLessThan(middlePoint);
            } else {
                predicate = mapNode -> !mapNode.isHorizontallyLessThan(middlePoint);
            }
        } else {
            if (position.isVerticallyLessThan(middlePoint)) {
                predicate = mapNode -> mapNode.isVerticallyLessThan(middlePoint);
            } else {
                predicate = mapNode -> !mapNode.isVerticallyLessThan(middlePoint);
            }
        }

        return predicate;
    }

    private Collection<GameMapNode> getHalfMapNodes(Position position) {
        Predicate<GameMapNode> isOnHalfMap = getHalfMapBinaryRelation(position);

        return getMapNodes(isOnHalfMap);
    }

    private Optional<GameMapNode> getPlayerFortMapNode() {
        return getMapNodes().stream()
                .filter(GameMapNode::hasPlayerFort)
                .findFirst();
    }

    private Optional<Position> getPlayerFortPosition() {
        return getPlayerFortMapNode().map(GameMapNode::getPosition);
    }

    public Collection<GameMapNode> getPlayerMapNodes() {
        // TODO: Improve error handling here
        return getHalfMapNodes(getPlayerFortPosition().orElseThrow());
    }

    public Collection<GameMapNode> getEnemyMapNodes() {
        // TODO: Improve error handling here
        Position playerFortPosition = getPlayerFortPosition().orElseThrow();

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

    public boolean anyMapNodeMatch(Predicate<GameMapNode> predicate) {
        return getMapNodes().stream().anyMatch(predicate);
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

                if (nodes.containsKey(currentPosition)) {
                    GameMapNode currentMapNode = nodes.get(currentPosition);
                    stringBuilder.append(String.format("%s", currentMapNode));
                } else {
                    stringBuilder.append(ANSIColor.format("a",
                                                          ANSIColor.BRIGHT_BLACK,
                                                          ANSIColor.BRIGHT_BLACK));
                }
            }
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }
}
