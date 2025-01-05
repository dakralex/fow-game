package client.search;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import static client.map.util.MapGenerationUtils.makeInaccessible;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.Path;
import client.map.Position;
import client.map.PositionArea;
import client.map.util.MapGenerationUtils;

class AStarPathFinderTest {

    private static final int HALF_MAP_X_SIZE = 10;
    private static final int HALF_MAP_Y_SIZE = 5;

    private static void assertStraightPath(GameMap map, Path path) {
        List<MapDirection> directions = path.intoMapDirections(map);

        Optional<MapDirection> maybeDirection = directions.stream().findFirst();

        assertTrue(maybeDirection.isPresent(), "Resulting path does not contain any vertices");

        MapDirection direction = maybeDirection.get();
        assertTrue(directions.stream().allMatch(direction::equals),
                   "Resulting path does not contain only one direction (i.e. is straight)");
    }

    @ParameterizedTest
    @ArgumentsSource(StraightPathArgumentsProvider.class)
    void EmptyGrassMap_findPath_shouldOutputStraightPath(Position source, Position destination) {
        GameMap map = MapGenerationUtils.generateEmptyGameMap(HALF_MAP_X_SIZE, HALF_MAP_Y_SIZE);

        PathFinder pathFinder = new AStarPathFinder(map.getMapNodes());
        Path path = pathFinder.findPath(source, destination);

        assertStraightPath(map, path);
    }

    @ParameterizedTest
    @ArgumentsSource(StraightPathArgumentsProvider.class)
    void BlockedGrassMap_findPath_shouldFailFindingPath(Position source, Position destination) {
        PositionArea mapArea = new PositionArea(0, 0, HALF_MAP_X_SIZE, HALF_MAP_Y_SIZE);

        Predicate<Position> predicate = mapArea::isMiddle;
        Predicate<GameMapNode> isMiddle = mapNode -> predicate.test(mapNode.getPosition());

        GameMap map = MapGenerationUtils.generateEmptyGameMap(HALF_MAP_X_SIZE,
                                                              HALF_MAP_Y_SIZE,
                                                              makeInaccessible,
                                                              isMiddle);

        PathFinder pathFinder = new AStarPathFinder(map.getMapNodes());
        assertThrows(PathNotFoundException.class, () -> pathFinder.findPath(source, destination));
    }

    private static class StraightPathArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            PositionArea mapArea = new PositionArea(0, 0, HALF_MAP_X_SIZE, HALF_MAP_Y_SIZE);

            Position upperLeft = mapArea.upperLeft();
            Position upperRight = mapArea.upperRight();
            Position lowerLeft = mapArea.lowerLeft();
            Position lowerRight = mapArea.lowerRight();

            return Stream.of(
                    arguments(upperLeft, upperRight),
                    arguments(upperRight, upperLeft),

                    arguments(upperLeft, lowerLeft),
                    arguments(lowerLeft, upperLeft),

                    arguments(lowerRight, lowerLeft),
                    arguments(lowerLeft, lowerRight),

                    arguments(lowerRight, upperRight),
                    arguments(upperRight, lowerRight)
            );
        }
    }
}