package client.map.util;

import java.util.Map;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.Position;
import client.map.PositionArea;
import client.map.TerrainType;

public class MapGenerationUtils {

    public static GameMap generateEmptyGameMap(int mapXSize, int mapYSize,
                                               MapDirection playerFortPosition) {
        PositionArea mapArea = new PositionArea(0, 0, mapXSize, mapYSize);
        Map<Position, GameMapNode> mapNodes = mapArea.intoPositionStream()
                .map(position -> new GameMapNode(position, TerrainType.GRASS))
                .collect(GameMap.mapCollector);

        Position fortPosition = switch (playerFortPosition) {
            case EAST -> new Position(mapXSize - 1, mapYSize / 2);
            case NORTH -> new Position(mapXSize / 2, 0);
            case SOUTH -> new Position(mapXSize / 2, mapYSize - 1);
            case WEST -> new Position(0, mapYSize / 2);
        };

        GameMapNode fortMapNode = mapNodes.get(fortPosition);
        fortMapNode.placePlayerFort();

        return new GameMap(mapNodes);
    }
}
