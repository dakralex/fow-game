package client.validation;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.Position;
import client.map.PositionArea;
import client.map.TerrainType;
import client.map.util.MapGenerationUtils;
import client.validation.util.NotificationAssertUtils;

class GameMapTerrainReachabilityValidatorTest {

    private static final int HALF_MAP_X_SIZE = 10;
    private static final int HALF_MAP_Y_SIZE = 5;

    private static final GameMapValidationRule validator = new GameMapTerrainReachabilityValidator();

    private static Function<GameMapNode, GameMapNode> makeIsolatedIsland(PositionArea mapArea) {
        Collection<Position> allPositions = mapArea.intoStream().toList();

        Collection<Position> borderNodesExceptCorners = Arrays.stream(MapDirection.values())
                .map(mapArea::getBorderPredicate)
                .flatMap(borderPredicate -> allPositions.stream().filter(borderPredicate))
                .toList();

        return mapNode -> {
            Position mapNodePosition = mapNode.getPosition();
            if (borderNodesExceptCorners.contains(mapNodePosition)) {
                return new GameMapNode(mapNode.getPosition(), TerrainType.WATER);
            } else {
                return mapNode;
            }
        };
    }

    @Test
    void EmptyMap_validate_shouldMarkAsValid() {
        GameMap map = MapGenerationUtils.generateEmptyGameMap(HALF_MAP_X_SIZE,
                                                              HALF_MAP_Y_SIZE,
                                                              MapDirection.EAST);

        NotificationAssertUtils.assertNoViolation(map, validator);
    }

    @Test
    void BigIslandMap_validate_shouldMarkAsInvalid() {
        PositionArea mapArea = new PositionArea(0, 0, HALF_MAP_X_SIZE, HALF_MAP_Y_SIZE);

        GameMap map = MapGenerationUtils.generateEmptyGameMap(HALF_MAP_X_SIZE,
                                                              HALF_MAP_Y_SIZE,
                                                              makeIsolatedIsland(mapArea),
                                                              GameMapNode::isLootable);

        NotificationAssertUtils.assertSomeViolation(map, validator);
    }
}