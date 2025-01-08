package client.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import client.map.comparator.LootabilityComparator;
import client.map.comparator.NeighborCountComparator;
import client.map.comparator.TaxicabDistanceComparator;
import client.util.ANSIColor;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerHalfMapNode;
import messagesbase.messagesfromserver.FullMap;

public class GameMap {

    public static final Collector<GameMapNode, ?, Map<Position, GameMapNode>> mapCollector =
            Collectors.toMap(GameMapNode::getPosition, Function.identity());

    private static final int FULL_MAP_SIZE = 100;

    private final Map<Position, GameMapNode> nodes;

    public GameMap(Map<Position, GameMapNode> mapNodes) {
        this.nodes = HashMap.newHashMap(mapNodes.size());
        this.nodes.putAll(mapNodes);
    }

    public GameMap(Stream<GameMapNode> mapNodeStream) {
        this(mapNodeStream.collect(mapCollector));
    }

    public GameMap(Collection<GameMapNode> mapNodes) {
        this(mapNodes.stream());
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

    public int getSize() {
        return nodes.size();
    }

    public PositionArea getArea() {
        Position minPosition = getPositions().stream().min(Position::compareTo).orElseThrow();
        Position maxPosition = getPositions().stream().max(Position::compareTo).orElseThrow();

        return new PositionArea(minPosition, maxPosition);
    }

    public Set<Position> getPositions() {
        return Collections.unmodifiableSet(nodes.keySet());
    }

    private Collection<Position> getPositions(Predicate<Position> predicate) {
        return getPositions().stream().filter(predicate).toList();
    }

    public Collection<Position> getPositionsByMapNode(Predicate<GameMapNode> predicate) {
        return getMapNodes(predicate).stream().map(GameMapNode::getPosition).toList();
    }

    public Collection<Position> getPositionsInSight(Position cameraPosition) {
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

    public Optional<GameMapNode> getNodeAt(Position position) {
        return Optional.ofNullable(nodes.get(position));
    }

    public Collection<GameMapNode> getMapNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    private Collection<GameMapNode> getMapNodes(Stream<Position> positions) {
        return positions.map(nodes::get).toList();
    }

    public Collection<GameMapNode> getMapNodes(Predicate<GameMapNode> predicate) {
        return getMapNodes().stream().filter(predicate).toList();
    }

    private Predicate<GameMapNode> getBorderPredicate(MapDirection direction) {
        return mapNode -> getArea().getBorderPredicate(direction).test(mapNode.getPosition());
    }

    public Collection<GameMapNode> getBorderNodes(MapDirection direction) {
        Predicate<GameMapNode> isOnBorder = getBorderPredicate(direction);

        return getMapNodes(isOnBorder);
    }

    private Collection<GameMapNode> getUnvisitedNodes() {
        return getMapNodes().stream()
                .filter(GameMapNode::isUnvisited)
                .filter(GameMapNode::isAccessible)
                .sorted(new LootabilityComparator())
                .toList();
    }

    private Optional<Position> getRandomUnvisitedPosition() {
        // TODO: Make 'random' to 'best' unvisited position OR parameterize randomness
        List<GameMapNode> mapNodes = new ArrayList<>(getUnvisitedNodes().stream().toList());
        Collections.shuffle(mapNodes);

        return mapNodes.stream()
                .findFirst()
                .map(GameMapNode::getPosition);
    }

    public Optional<Position> getRandomUnvisitedDeadEndPosition() {
        List<GameMapNode> mapNodes = getUnvisitedNodes().stream()
                .sorted(new NeighborCountComparator(this))
                .toList();

        return mapNodes.stream()
                .findFirst()
                .map(GameMapNode::getPosition)
                .or(this::getRandomUnvisitedPosition);
    }

    public Optional<Position> getRandomNearbyLootablePosition(Position source) {
        Comparator<Position> distanceComparator = new TaxicabDistanceComparator(source);
        List<Position> lootableNodes = new ArrayList<>();
        Collection<Position> nearbyLootableNodes = new ArrayList<>();

        do {
            nearbyLootableNodes.clear();
            nearbyLootableNodes.addAll(
                    getReachableNeighbors(source)
                            .stream()
                            .filter(GameMapNode::isUnvisited)
                            .filter(GameMapNode::isLootable)
                            .map(GameMapNode::getPosition)
                            .filter(position -> !lootableNodes.contains(position))
                            .sorted(distanceComparator)
                            .toList());
            lootableNodes.addAll(nearbyLootableNodes);
        } while (!nearbyLootableNodes.isEmpty());

        Collections.shuffle(lootableNodes);

        return lootableNodes.stream().findFirst();
    }

    public Optional<Position> getPlayerFortPosition() {
        return getMapNodes(GameMapNode::hasPlayerFort).stream().findFirst()
                .map(GameMapNode::getPosition);
    }

    public GameMap getPlayerHalfMap() {
        // TODO: Improve error handling here
        Position playerFortPosition = getPlayerFortPosition().orElseThrow();

        return new GameMap(getMapNodes(getArea().intoCurrentHalfStream(playerFortPosition)));
    }

    public GameMap getEnemyHalfMap() {
        // TODO: Improve error handling here
        Position playerFortPosition = getPlayerFortPosition().orElseThrow();

        // Retrieve the enemy's half map nodes with respect to the player's fort position,
        // since the enemy's fort position is most likely not known yet
        return new GameMap(getMapNodes(getArea().intoOtherHalfStream(playerFortPosition)));
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

    public boolean anyMapNodeMatch(Predicate<GameMapNode> predicate) {
        return getMapNodes().stream().anyMatch(predicate);
    }

    public boolean isFullMap() {
        return getSize() == FULL_MAP_SIZE;
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
                Optional<GameMapNode> mapNode = getNodeAt(new Position(x, y));

                if (mapNode.isPresent()) {
                    stringBuilder.append(String.format("%s", mapNode.get()));
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
