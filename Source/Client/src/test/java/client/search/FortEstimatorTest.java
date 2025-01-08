package client.search;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.Position;
import client.map.TerrainType;
import client.map.util.MapGenerationUtils;
import client.player.Player;
import client.player.PlayerDetails;
import client.player.PlayerGameState;

class FortEstimatorTest {

    private static final int HALF_MAP_X_SIZE = 10;
    private static final int HALF_MAP_Y_SIZE = 5;

    private static final String enemyName = "enemy";
    private static final PlayerDetails enemyDetails = new PlayerDetails("Enemy",
                                                                        "Ymeny",
                                                                        enemyName);

    @Test
    void NoKnowledge_getPossiblePositions_shouldOutputEmpty() {
        GameMap map = MapGenerationUtils.generateEmptyGameMap(HALF_MAP_X_SIZE,
                                                              HALF_MAP_Y_SIZE,
                                                              MapDirection.EAST);
        Player enemy = new Player(enemyName,
                                  enemyDetails,
                                  PlayerGameState.MUST_ACT,
                                  new Position(0, 0),
                                  false);

        FortEstimator fortEstimator = new FortEstimator(map, enemy);

        assertEquals(Collections.emptyList(),
                     fortEstimator.getPossiblePositions(0),
                     "Estimator should output empty without any knowledge");
    }

    @ParameterizedTest
    @ArgumentsSource(BaseCasesArgumentsProvider.class)
    void BaseCases_getPossiblePositions_shouldOutputPerfectLocation(int currentTurn,
                                                                    String terrainTypeString,
                                                                    Position enemyPosition) {
        List<TerrainType> terrainTypes = terrainTypeString.chars()
                .mapToObj(terrainTypeChar -> switch (terrainTypeChar) {
                    case 'G' -> TerrainType.GRASS;
                    case 'M' -> TerrainType.MOUNTAIN;
                    default -> throw new IllegalArgumentException("Invalid terrain type string");
                }).toList();
        Collection<GameMapNode> mapNodes = IntStream.range(0, 5)
                .mapToObj(index -> new GameMapNode(new Position(index, 0), terrainTypes.get(index)))
                .toList();

        GameMap map = new GameMap(mapNodes);
        Player enemy = new Player(enemyName,
                                  enemyDetails,
                                  PlayerGameState.MUST_ACT,
                                  enemyPosition,
                                  false);

        FortEstimator fortEstimator = new FortEstimator(map, enemy);

        assertEquals(new Position(0, 0),
                     fortEstimator.getPossiblePositions(currentTurn).stream().sorted().findFirst().get(),
                     "Estimated position should match the origin position");
    }

    private static class BaseCasesArgumentsProvider implements ArgumentsProvider {

        static final Position THREE_STEPS = new Position(2, 0);
        static final Position FOUR_STEPS = new Position(3, 0);
        static final Position FIVE_STEPS = new Position(4, 0);

        // @formatter:off
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    arguments(8,  "GGGGG", FIVE_STEPS),
                    arguments(9,  "GGGGM", FIVE_STEPS),
                    arguments(10, "GGGMG", FIVE_STEPS),
                    arguments(11, "GGGMM", FIVE_STEPS),
                    arguments(8,  "GGMGG", FOUR_STEPS),
                    arguments(8,  "GGMGM", FOUR_STEPS),
                    arguments(9,  "GGMMG", FOUR_STEPS),
                    arguments(9,  "GGMMM", FOUR_STEPS),
                    arguments(8,  "GMGGG", FOUR_STEPS),
                    arguments(8,  "GMGGM", FOUR_STEPS),
                    arguments(9,  "GMGMG", FOUR_STEPS),
                    arguments(9,  "GMGMM", FOUR_STEPS),
                    arguments(10, "GMMGG", FOUR_STEPS),
                    arguments(10, "GMMGM", FOUR_STEPS),
                    arguments(11, "GMMMG", FOUR_STEPS),
                    arguments(11, "GMMMM", FOUR_STEPS),
                    arguments(9,  "MGGGG", FIVE_STEPS),
                    arguments(10, "MGGGM", FIVE_STEPS),
                    arguments(8,  "MGGMG", FOUR_STEPS),
                    arguments(8,  "MGGMM", FOUR_STEPS),
                    arguments(9,  "MGMGG", FOUR_STEPS),
                    arguments(9,  "MGMGM", FOUR_STEPS),
                    arguments(10, "MGMMG", FOUR_STEPS),
                    arguments(10, "MGMMM", FOUR_STEPS),
                    arguments(9,  "MMGGG", FOUR_STEPS),
                    arguments(9,  "MMGGM", FOUR_STEPS),
                    arguments(10, "MMGMG", FOUR_STEPS),
                    arguments(10, "MMGMM", FOUR_STEPS),
                    arguments(8,  "MMMGG", THREE_STEPS),
                    arguments(8,  "MMMGM", THREE_STEPS),
                    arguments(8,  "MMMMG", THREE_STEPS),
                    arguments(8,  "MMMMM", THREE_STEPS)
            );
        }
        // @formatter:on
    }
}