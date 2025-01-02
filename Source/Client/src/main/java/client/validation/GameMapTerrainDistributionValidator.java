package client.validation;

import java.util.Collections;
import java.util.List;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.TerrainType;

public class GameMapTerrainDistributionValidator implements GameMapValidationRule {

    private static final int GRASS_MIN_AMOUNT = 24;
    private static final int MOUNTAIN_MIN_AMOUNT = 5;
    private static final int WATER_MIN_AMOUNT = 7;

    @Override
    public boolean validate(GameMap map) {
        List<TerrainType> terrains = map.getMapNodes().stream()
                .map(GameMapNode::getTerrainType)
                .toList();

        int grassCount = Collections.frequency(terrains, TerrainType.GRASS);
        int mountainCount = Collections.frequency(terrains, TerrainType.MOUNTAIN);
        int waterCount = Collections.frequency(terrains, TerrainType.WATER);

        return grassCount >= GRASS_MIN_AMOUNT
                && mountainCount >= MOUNTAIN_MIN_AMOUNT
                && waterCount >= WATER_MIN_AMOUNT;
    }
}
