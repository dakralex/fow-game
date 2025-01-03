package client.map;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

class GameMapTest {

    private static final int HALF_MAP_X_SIZE = 10;
    private static final int HALF_MAP_Y_SIZE = 5;
    private static final int HALF_MAP_SIZE = HALF_MAP_X_SIZE * HALF_MAP_Y_SIZE;

    private static final int HORIZONTAL_MAP_X_SIZE = 2 * HALF_MAP_X_SIZE;
    private static final int HORIZONTAL_MAP_Y_SIZE = HALF_MAP_Y_SIZE;

    private static final int VERTICAL_MAP_X_SIZE = HALF_MAP_X_SIZE;
    private static final int VERTICAL_MAP_Y_SIZE = 2 * HALF_MAP_Y_SIZE;

    private static GameMap generateEmptyGameMap(int mapXSize, int mapYSize,
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

    @ParameterizedTest
    @ArgumentsSource(FullMapFortHalfMapArgumentsProvider.class)
    void FullMap_getPlayerMapNodes_shouldReturnCorrectHalfMapNodes(int mapXSize,
                                                                   int mapYSize,
                                                                   MapDirection fortPlacement,
                                                                   PositionArea expectedArea) {
        GameMap map = generateEmptyGameMap(mapXSize, mapYSize, fortPlacement);
        Collection<Position> mapNodePositions = map.getPlayerMapNodes().stream()
                .map(GameMapNode::getPosition)
                .sorted().toList();

        Collection<Position> areaPositions = expectedArea.intoPositionStream().sorted().toList();

        assertEquals(HALF_MAP_SIZE,
                     mapNodePositions.size(),
                     "Player half map should equal the expected size");
        assertArrayEquals(areaPositions.toArray(),
                          mapNodePositions.toArray(),
                          "Player half map nodes should equal the expected half map nodes");
    }

    @ParameterizedTest
    @ArgumentsSource(FullMapFortHalfMapArgumentsProvider.class)
    void FullMap_getEnemyMapNodes_shouldReturnCorrectHalfMapNodes(int mapXSize, int mapYSize,
                                                                  MapDirection oppositeFortPlacement,
                                                                  PositionArea expectedArea) {
        MapDirection fortPlacement = oppositeFortPlacement.getOpposite();

        GameMap map = generateEmptyGameMap(mapXSize, mapYSize, fortPlacement);
        Collection<Position> mapNodePositions = map.getEnemyMapNodes().stream()
                .map(GameMapNode::getPosition)
                .sorted().toList();

        Collection<Position> areaPositions = expectedArea.intoPositionStream().sorted().toList();

        assertEquals(HALF_MAP_SIZE,
                     mapNodePositions.size(),
                     "Enemy half map should equal the expected size");
        assertArrayEquals(areaPositions.toArray(),
                          mapNodePositions.toArray(),
                          "Enemy half map nodes should equal the expected half map nodes");
    }

    private static class FullMapFortHalfMapArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            // Horizontal west / Vertical north half map
            PositionArea defaultArea = new PositionArea(0, 0, HALF_MAP_X_SIZE, HALF_MAP_Y_SIZE);
            // Vertical south half map
            PositionArea verticalSouthArea = new PositionArea(0,
                                                              HALF_MAP_Y_SIZE,
                                                              HALF_MAP_X_SIZE,
                                                              HALF_MAP_Y_SIZE);
            // Horizontal east half map
            PositionArea horizontalEastArea = new PositionArea(HALF_MAP_X_SIZE,
                                                               0,
                                                               HALF_MAP_X_SIZE,
                                                               HALF_MAP_Y_SIZE);

            return Stream.of(
                    // Horizontal full map + Player fort in west half -> west half map
                    arguments(HORIZONTAL_MAP_X_SIZE,
                              HORIZONTAL_MAP_Y_SIZE,
                              MapDirection.WEST,
                              defaultArea),
                    // Horizontal full map + Player fort in east half -> east half map
                    arguments(HORIZONTAL_MAP_X_SIZE,
                              HORIZONTAL_MAP_Y_SIZE,
                              MapDirection.EAST,
                              horizontalEastArea),
                    // Vertical full map + Player fort in north half -> north half map
                    arguments(VERTICAL_MAP_X_SIZE,
                              VERTICAL_MAP_Y_SIZE,
                              MapDirection.NORTH,
                              defaultArea),
                    // Vertical full map + Player fort in south half -> south half map
                    arguments(VERTICAL_MAP_X_SIZE,
                              VERTICAL_MAP_Y_SIZE,
                              MapDirection.SOUTH,
                              verticalSouthArea)
            );
        }
    }
}