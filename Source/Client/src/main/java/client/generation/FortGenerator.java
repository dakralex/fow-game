package client.generation;

import java.util.HashMap;
import java.util.Map;

import client.map.FortState;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.Position;
import client.map.TerrainType;

public class FortGenerator {

    private static final int FORT_STRUCTURE_SIZE = 10;

    /**
     * Generates a fort structure, which is placed at the given {@code fortPosition}, oriented to
     * have the entrance/exit in {@code entryDirection}, and should act as the base for the map
     * generation to have a good starting location in one's own half map.
     *
     * @param fortPosition   the position of the fort map node
     * @param entryDirection the direction to orient the fort structure to
     * @return the map nodes of the fort structure
     */
    public Map<Position, GameMapNode> generateFortStructure(Position fortPosition,
                                                            MapDirection entryDirection) {
        Map<Position, GameMapNode> mapNodes = HashMap.newHashMap(FORT_STRUCTURE_SIZE);

        GameMapNode fortMapNode = new GameMapNode(fortPosition,
                                                  TerrainType.GRASS,
                                                  FortState.PLAYER_FORT_PRESENT);

        /*
         * The generated fort structure looks like the following (wrt. to the directions):
         *
         *                      ^ blockDirection
         *                    a W W G
         * entryDirection <-- W G F W --> wallDirection
         *                    a G W G
         *                      v pathDirection
         */

        MapDirection blockDirection = entryDirection.getPerpendicular();
        MapDirection wallDirection = entryDirection.getOpposite();
        MapDirection pathDirection = blockDirection.getOpposite();

        Position entryPos1 = fortPosition.stepInDirection(entryDirection);
        Position entryPos2 = entryPos1.stepInDirection(entryDirection);
        Position entryPathPos = entryPos1.stepInDirection(pathDirection);
        Position entryBlockPos = entryPos1.stepInDirection(blockDirection);

        Position blockPos = fortPosition.stepInDirection(blockDirection);
        Position blockWallPos = blockPos.stepInDirection(wallDirection);

        Position wallPos = fortPosition.stepInDirection(wallDirection);
        Position wallPathPos = wallPos.stepInDirection(pathDirection);

        Position pathPos = fortPosition.stepInDirection(pathDirection);

        mapNodes.put(entryBlockPos, new GameMapNode(entryBlockPos, TerrainType.WATER));
        mapNodes.put(blockPos, new GameMapNode(blockPos, TerrainType.WATER));
        mapNodes.put(blockWallPos, new GameMapNode(blockWallPos, TerrainType.GRASS));
        mapNodes.put(entryPos2, new GameMapNode(entryPos2, TerrainType.WATER));
        mapNodes.put(entryPos1, new GameMapNode(entryPos1, TerrainType.GRASS));
        mapNodes.put(fortPosition, fortMapNode);
        mapNodes.put(wallPos, new GameMapNode(wallPos, TerrainType.WATER));
        mapNodes.put(entryPathPos, new GameMapNode(entryPathPos, TerrainType.GRASS));
        mapNodes.put(pathPos, new GameMapNode(pathPos, TerrainType.WATER));
        mapNodes.put(wallPathPos, new GameMapNode(wallPathPos, TerrainType.GRASS));

        return mapNodes;
    }
}
