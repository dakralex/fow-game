package client.generation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.Position;
import client.map.PositionArea;
import client.map.TerrainType;
import client.validation.GameMapValidationRule;
import client.validation.HalfMapValidator;
import client.validation.Notification;

public class MapGenerator {

    private static final Logger logger = LoggerFactory.getLogger(MapGenerator.class);

    private static final int X_SIZE = 10;
    private static final int Y_SIZE = 5;
    private static final int MAP_SIZE = X_SIZE * Y_SIZE;

    private static final int X_FORT_BORDER_SIZE = 2;
    private static final int Y_FORT_BORDER_SIZE = 2;

    private final Random random;
    private final FortGenerator fortGenerator;
    private final TerrainDistributionGenerator terrainGenerator;

    public MapGenerator() {
        this.random = new Random();
        this.fortGenerator = new FortGenerator();
        this.terrainGenerator = new TerrainDistributionGenerator();
    }

    public MapGenerator(long seed) {
        this.random = new Random(seed);
        this.fortGenerator = new FortGenerator();
        this.terrainGenerator = new TerrainDistributionGenerator();
    }

    private Position pickFortPosition() {
        int x = random.nextInt(X_FORT_BORDER_SIZE, X_SIZE - X_FORT_BORDER_SIZE);
        int y = random.nextInt(Y_FORT_BORDER_SIZE, Y_SIZE - X_FORT_BORDER_SIZE);

        return new Position(x, y);
    }

    private static Set<Position> generatePositionRange() {
        PositionArea mapArea = new PositionArea(0, 0, X_SIZE, Y_SIZE);

        return mapArea.intoPositionStream().collect(Collectors.toSet());
    }

    private GameMap generateMap() {
        Map<Position, GameMapNode> mapNodes = HashMap.newHashMap(MAP_SIZE);

        Position fortPosition = pickFortPosition();
        MapDirection fortEntranceDirection = MapDirection.randomDirection(random);
        Map<Position, GameMapNode> fortStructureNodes = fortGenerator
                .generateFortStructure(fortPosition, fortEntranceDirection);

        mapNodes.putAll(fortStructureNodes);

        int remainingMapNodes = MAP_SIZE - mapNodes.size();

        Set<Position> positionQueue = generatePositionRange();
        positionQueue.removeAll(mapNodes.keySet());

        List<TerrainType> terrainTypeQueue = terrainGenerator.generateTerrainQueue(remainingMapNodes);
        Collections.shuffle(terrainTypeQueue, random);

        Map<Position, GameMapNode> randomMapNodes = positionQueue.stream()
                .map(position -> new GameMapNode(position, terrainTypeQueue.removeLast()))
                .collect(GameMap.mapCollector);

        mapNodes.putAll(randomMapNodes);

        return new GameMap(mapNodes.values());
    }

    public GameMap generateUntilValid(HalfMapValidator validator) {
        int generateTries = 0;
        GameMap map;
        Notification<GameMapValidationRule> validationErrors;

        do {
            ++generateTries;
            map = generateMap();
            validationErrors = validator.validate(map);

            if (validationErrors.hasEntries()) {
                logger.debug("The generated half map violates the following rules:\n{}",
                             validationErrors);
            }
        } while(validationErrors.hasEntries());

        logger.debug("It took {} tries to generate a valid GameMap instance.", generateTries);

        return map;
    }
}
