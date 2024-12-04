package client.generation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.Position;
import client.map.TerrainType;
import client.validation.HalfMapValidator;

public class MapGenerator {

    private static final Logger logger = LoggerFactory.getLogger(MapGenerator.class);

    private static final int X_SIZE = 10;
    private static final int Y_SIZE = 5;
    private static final int MAP_SIZE = X_SIZE * Y_SIZE;

    private static final int X_FORT_BORDER_SIZE = 2;
    private static final int Y_FORT_BORDER_SIZE = 2;

    private static final int GRASS_MIN_AMOUNT = 24;
    private static final int MOUNTAIN_MIN_AMOUNT = 5;
    private static final int WATER_MIN_AMOUNT = 7;

    private final Random random;
    private final FortGenerator fortGenerator;

    public MapGenerator() {
        this.random = new Random();
        this.fortGenerator = new FortGenerator();
    }

    private Position pickFortPosition() {
        int x = random.nextInt(X_FORT_BORDER_SIZE, X_SIZE - X_FORT_BORDER_SIZE);
        int y = random.nextInt(Y_FORT_BORDER_SIZE, Y_SIZE - X_FORT_BORDER_SIZE);

        return new Position(x, y);
    }

    private Set<Position> generatePositionRange() {
        return IntStream.range(0, MAP_SIZE)
                .mapToObj(index -> new Position(index % X_SIZE, Math.floorDiv(index, X_SIZE)))
                .collect(Collectors.toSet());
    }

    private List<TerrainType> generateTerrainTypeQueue(int amount) {
        List<TerrainType> terrainTypeQueue = new ArrayList<>(amount);

        terrainTypeQueue.addAll(Collections.nCopies(GRASS_MIN_AMOUNT, TerrainType.GRASS));
        terrainTypeQueue.addAll(Collections.nCopies(MOUNTAIN_MIN_AMOUNT, TerrainType.MOUNTAIN));
        terrainTypeQueue.addAll(Collections.nCopies(WATER_MIN_AMOUNT, TerrainType.WATER));

        int remainingAmount = amount - terrainTypeQueue.size();
        terrainTypeQueue.addAll(Collections.nCopies(remainingAmount, TerrainType.MOUNTAIN));

        return terrainTypeQueue;
    }

    public GameMap generateMap() {
        Map<Position, GameMapNode> mapNodes = HashMap.newHashMap(MAP_SIZE);

        Position fortPosition = pickFortPosition();
        MapDirection fortEntranceDirection = MapDirection.randomDirection(random);
        Map<Position, GameMapNode> fortStructureNodes = fortGenerator
                .generateFortStructure(fortPosition, fortEntranceDirection);

        mapNodes.putAll(fortStructureNodes);

        int remainingMapNodes = MAP_SIZE - mapNodes.size();

        Set<Position> positionQueue = generatePositionRange();
        positionQueue.removeAll(mapNodes.keySet());

        List<TerrainType> terrainTypeQueue = generateTerrainTypeQueue(remainingMapNodes);
        Collections.shuffle(terrainTypeQueue, random);

        Map<Position, GameMapNode> randomMapNodes = positionQueue.stream()
                .map(position -> new GameMapNode(position, terrainTypeQueue.removeLast()))
                .collect(GameMap.mapCollector);

        mapNodes.putAll(randomMapNodes);

        return new GameMap(mapNodes.values());
    }

    public GameMap generateUntilValid(HalfMapValidator validator) {
        int generateTries = 0;
        GameMap map = generateMap();

        while (!validator.validate(map)) {
            map = generateMap();
            ++generateTries;
        }

        logger.debug("It took {} tries to generate a valid GameMap instance.", generateTries);

        return map;
    }
}
