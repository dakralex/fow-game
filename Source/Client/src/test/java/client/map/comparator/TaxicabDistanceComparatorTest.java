package client.map.comparator;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.Comparator;
import java.util.stream.Stream;

import client.map.Position;

class TaxicabDistanceComparatorTest {

    private static <T> void assertSmaller(Comparator<T> comparator, T o1, T o2) {
        int result = comparator.compare(o1, o2);
        int normalizedResult = result / Math.abs(result);

        assertEquals(-1,
                     result == 0 ? 0 : normalizedResult,
                     "First argument should be smaller than second argument");
    }

    @ParameterizedTest
    @ArgumentsSource(SmallerPositionsArgumentsProvider.class)
    void FartherAwayPositions_compare_shouldReturnTrue(Position source, Position closePosition,
                                                       Position farPosition) {
        assertSmaller(new TaxicabDistanceComparator(source), closePosition, farPosition);
    }

    private static class SmallerPositionsArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            Position startingPosition = new Position(5, 5);

            return Stream.of(
                    arguments(startingPosition, new Position(3, 5), new Position(0, 5)),
                    arguments(startingPosition, new Position(3, 3), new Position(0, 0)),
                    arguments(startingPosition, new Position(5, 3), new Position(5, 0)),

                    arguments(startingPosition, new Position(7, 5), new Position(10, 5)),
                    arguments(startingPosition, new Position(7, 7), new Position(10, 10)),
                    arguments(startingPosition, new Position(5, 7), new Position(5, 10))
            );
        }
    }
}