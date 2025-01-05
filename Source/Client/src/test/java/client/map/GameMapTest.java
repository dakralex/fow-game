package client.map;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import static client.map.util.MapGenerationUtils.generateEmptyGameMap;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

class GameMapTest {

    private static final int HALF_MAP_X_SIZE = 10;
    private static final int HALF_MAP_Y_SIZE = 5;
    private static final int HALF_MAP_SIZE = HALF_MAP_X_SIZE * HALF_MAP_Y_SIZE;

    private static final int HORIZONTAL_MAP_X_SIZE = 2 * HALF_MAP_X_SIZE;
    private static final int HORIZONTAL_MAP_Y_SIZE = HALF_MAP_Y_SIZE;

    private static final int VERTICAL_MAP_X_SIZE = HALF_MAP_X_SIZE;
    private static final int VERTICAL_MAP_Y_SIZE = 2 * HALF_MAP_Y_SIZE;

    private static final int SMALL_MAP_X_SIZE = 3;
    private static final int SMALL_MAP_Y_SIZE = 3;

    private static void assertSameVisiblePositions(Collection<Position> expectedPositions,
                                                   Collection<Position> actualPositions) {
        assertArrayEquals(expectedPositions.stream().sorted().toArray(),
                          actualPositions.stream().sorted().toArray(),
                          "There should be the expected set of visible positions");
    }

    @ParameterizedTest
    @CsvSource({"0,0", "1,0", "2,0", "0,1", "1,1", "2,1", "0,2", "1,2", "2,2"})
    void SmallGrassMaps_getPositionsInSight_shouldReturnItself(int grassPosX, int grassPosY) {
        GameMap map = generateEmptyGameMap(SMALL_MAP_X_SIZE, SMALL_MAP_Y_SIZE);

        Position cameraPosition = new Position(grassPosX, grassPosY);
        Collection<Position> visiblePositions = map.getPositionsInSight(cameraPosition);

        assertSameVisiblePositions(List.of(cameraPosition), visiblePositions);
    }

    @ParameterizedTest
    @ArgumentsSource(SmallMountainMapArgumentsProvider.class)
    void SmallMountainMaps_getPositionsInSight_shouldReturnAllVisible(Position mountainPosition,
                                                                      Collection<Position> expectedPositions) {
        GameMap map = generateEmptyGameMap(SMALL_MAP_X_SIZE, SMALL_MAP_Y_SIZE, mapNodes -> {
            GameMapNode mountainNode = mapNodes.get(mountainPosition);
            mountainNode.setTerrainType(TerrainType.MOUNTAIN);
        });

        Collection<Position> visiblePositions = map.getPositionsInSight(mountainPosition);

        assertSameVisiblePositions(expectedPositions, visiblePositions);
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

    private static class SmallMountainMapArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    arguments(new Position(0, 0), List.of(
                            new Position(0, 0), new Position(1, 0),
                            new Position(0, 1), new Position(1, 1)
                    )),
                    arguments(new Position(1, 0), List.of(
                            new Position(0, 0), new Position(1, 0), new Position(2, 0),
                            new Position(0, 1), new Position(1, 1), new Position(2, 1)
                    )),
                    arguments(new Position(2, 0), List.of(
                            new Position(1, 0), new Position(2, 0),
                            new Position(1, 1), new Position(2, 1)
                    )),
                    arguments(new Position(0, 1), List.of(
                            new Position(0, 0), new Position(1, 0),
                            new Position(0, 1), new Position(1, 1),
                            new Position(0, 2), new Position(1, 2)
                    )),
                    arguments(new Position(1, 1), List.of(
                            new Position(0, 0), new Position(1, 0), new Position(2, 0),
                            new Position(0, 1), new Position(1, 1), new Position(2, 1),
                            new Position(0, 2), new Position(1, 2), new Position(2, 2)
                    )),
                    arguments(new Position(2, 1), List.of(
                            new Position(1, 0), new Position(2, 0),
                            new Position(1, 1), new Position(2, 1),
                            new Position(1, 2), new Position(2, 2)
                    )),
                    arguments(new Position(0, 2), List.of(
                            new Position(0, 1), new Position(1, 1),
                            new Position(0, 2), new Position(1, 2)
                    )),
                    arguments(new Position(1, 2), List.of(
                            new Position(0, 1), new Position(1, 1), new Position(2, 1),
                            new Position(0, 2), new Position(1, 2), new Position(2, 2)
                    )),
                    arguments(new Position(2, 2), List.of(
                            new Position(1, 1), new Position(2, 1),
                            new Position(1, 2), new Position(2, 2)
                    ))
            );
        }
    }
}