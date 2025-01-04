package client.map.util;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.Position;
import client.map.PositionArea;
import client.map.TerrainType;

public class MapGenerationUtils {

    private static final Consumer<Map<Position, GameMapNode>> dummyConsumer =
            mapNodes -> {};

    public static GameMap generateEmptyGameMap(int mapXSize, int mapYSize,
                                               Consumer<Map<Position, GameMapNode>> mapper) {
        PositionArea mapArea = new PositionArea(0, 0, mapXSize, mapYSize);
        Map<Position, GameMapNode> mapNodes = mapArea.intoPositionStream()
                .map(position -> new GameMapNode(position, TerrainType.GRASS))
                .collect(GameMap.mapCollector);

        mapper.accept(mapNodes);

        return new GameMap(mapNodes);
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

    public static GameMap changeGameMapNodes(GameMap map, Iterable<GameMapNode> mapNodes,
                                             Function<GameMapNode, GameMapNode> mapper) {
        Map<Position, GameMapNode> allMapNodes = map.getMapNodes().stream()
                .collect(GameMap.mapCollector);

        mapNodes.forEach(mapNode ->
                                 allMapNodes.replace(mapNode.getPosition(), mapper.apply(mapNode)));

        return new GameMap(allMapNodes);
    }
}
