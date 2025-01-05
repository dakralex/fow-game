package client.map.util;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.Position;
import client.map.PositionArea;
import client.map.TerrainType;

public class MapGenerationUtils {

    private static final Consumer<Map<Position, GameMapNode>> dummyConsumer =
            mapNodes -> {};

    public static final Function<GameMapNode, GameMapNode> makeInaccessible =
                    mapNode -> new GameMapNode(mapNode.getPosition(), TerrainType.WATER);

    private static Map<Position, GameMapNode> generateEmptyGameMapNodes(int mapXSize,
                                                                        int mapYSize) {
        PositionArea mapArea = new PositionArea(0, 0, mapXSize, mapYSize);

        return mapArea.intoStream()
                .map(position -> new GameMapNode(position, TerrainType.GRASS))
                .collect(GameMap.mapCollector);
    }

    public static GameMap generateEmptyGameMap(int mapXSize, int mapYSize,
                                               Consumer<Map<Position, GameMapNode>> manipulator) {
        Map<Position, GameMapNode> mapNodes = generateEmptyGameMapNodes(mapXSize, mapYSize);

        manipulator.accept(mapNodes);

        return new GameMap(mapNodes);
    }

    /**
     * Generate an empty {@link GameMap} instance (i.e. only grass fields) with a manipulating
     * {@code mapper} function changing some {@link GameMapNode}s.
     *
     * @param mapXSize the width of the game map
     * @param mapYSize the height of the game map
     * @param mapper the manipulating method to change existing map nodes
     * @param condition the condition which map nodes should be changed
     * @param maxCount the maximum count that should be selected with the condition
     * @return the generated game map
     */
    public static GameMap generateEmptyGameMap(int mapXSize, int mapYSize,
                                               Function<GameMapNode, GameMapNode> mapper,
                                               Predicate<GameMapNode> condition, long maxCount) {
        Map<Position, GameMapNode> mapNodes = generateEmptyGameMapNodes(mapXSize, mapYSize);

        Map<Position, GameMapNode> newMapNodes = mapNodes.values().stream()
                .filter(condition)
                .limit(maxCount)
                .map(mapper)
                .collect(GameMap.mapCollector);

        mapNodes.putAll(newMapNodes);

        return new GameMap(mapNodes);
    }

    public static GameMap generateEmptyGameMap(int mapXSize, int mapYSize,
                                               Function<GameMapNode, GameMapNode> mapper,
                                               Predicate<GameMapNode> condition) {
        return generateEmptyGameMap(mapXSize, mapYSize, mapper, condition, Long.MAX_VALUE);
    }

    public static GameMap generateEmptyGameMap(int mapXSize, int mapYSize) {
        return generateEmptyGameMap(mapXSize, mapYSize, dummyConsumer);
    }

    public static GameMap generateEmptyGameMap(int mapXSize, int mapYSize,
                                               MapDirection playerFortPosition) {
        return generateEmptyGameMap(mapXSize, mapYSize, mapNodes -> {
            Position fortPosition = switch (playerFortPosition) {
                case EAST -> new Position(mapXSize - 1, mapYSize / 2);
                case NORTH -> new Position(mapXSize / 2, 0);
                case SOUTH -> new Position(mapXSize / 2, mapYSize - 1);
                case WEST -> new Position(0, mapYSize / 2);
            };

            GameMapNode fortMapNode = mapNodes.get(fortPosition);
            fortMapNode.placePlayerFort();
        });
    }
}
