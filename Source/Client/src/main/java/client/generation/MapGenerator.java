package client.generation;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.Position;
import client.validation.HalfMapValidator;

public class MapGenerator {

    private static final int X_SIZE = 10;
    private static final int Y_SIZE = 5;
    private static final int MAP_SIZE = X_SIZE * Y_SIZE;

    private static final int X_FORT_BORDER_SIZE = 2;
    private static final int Y_FORT_BORDER_SIZE = 2;

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

    public GameMap generateMap() {
        // TODO: Implement a complete map generation algorithm
        Map<Position, GameMapNode> mapNodes = HashMap.newHashMap(MAP_SIZE);

        Position fortPosition = pickFortPosition();
        MapDirection fortEntranceDirection = MapDirection.randomDirection(random);
        mapNodes.putAll(fortGenerator.generateFortStructure(fortPosition, fortEntranceDirection));

        return new GameMap(mapNodes.values());
    }

    public GameMap generateUntilValid(HalfMapValidator validator) {
        GameMap map = generateMap();

        while (!validator.validate(map)) {
            map = generateMap();
        }

        return map;
    }
}
