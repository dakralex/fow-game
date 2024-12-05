package client.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public class Path {

    List<Position> vertices;

    public Path(Collection<Position> vertices) {
        this.vertices = new ArrayList<>(vertices);
    }

    private static List<GameMapNode> mapVerticesToNodes(GameMap map, List<Position> vertices) {
        return vertices.stream()
                .map(map::getNodeAt)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private static IntFunction<Step> getNodesToEdgesMapper(List<GameMapNode> nodeVertices) {
        return index -> {
            GameMapNode currentNode = nodeVertices.get(index);
            GameMapNode nextNode = nodeVertices.get(index + 1);

            Position currentPosition = currentNode.getPosition();
            Position nextPosition = nextNode.getPosition();

            MapDirection direction = currentPosition.getDirection(nextPosition);

            return new Step(direction, currentNode.getTerrainType(), nextNode.getTerrainType());
        };
    }

    private static List<Step> mapNodesToSteps(List<GameMapNode> nodeVertices) {
        IntStream edgeIterator = IntStream.range(0, nodeVertices.size() - 1);
        IntFunction<Step> mapNodeToEdge = getNodesToEdgesMapper(nodeVertices);

        return edgeIterator.mapToObj(mapNodeToEdge).toList();
    }

    private static void expandStepsToDirections(Step step, Consumer<MapDirection> consumer) {
        for (int i = 0; i < step.getTravelCost(); ++i) {
            consumer.accept(step.direction());
        }
    }

    public List<MapDirection> intoMapDirections(GameMap map) {
        List<Step> steps = mapNodesToSteps(mapVerticesToNodes(map, vertices));

        return new ArrayList<>(steps.stream().mapMulti(Path::expandStepsToDirections).toList());
    }

    @Override
    public String toString() {
        return vertices.toString();
    }
}
