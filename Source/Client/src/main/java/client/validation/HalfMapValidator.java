package client.validation;

import java.util.Collections;
import java.util.List;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.PositionArea;
import client.map.TerrainType;

public class HalfMapValidator {

    private static final int X_SIZE = 10;
    private static final int Y_SIZE = 5;
    private static final int MAP_SIZE = X_SIZE * Y_SIZE;

    private static final int GRASS_MIN_AMOUNT = 24;
    private static final int MOUNTAIN_MIN_AMOUNT = 5;
    private static final int WATER_MIN_AMOUNT = 7;

    private static final int BORDER_ACCESSIBLE_MIN_AMOUNT = 14;

    private boolean validateSize(GameMap map) {
        return map.getSize() == MAP_SIZE;
    }

    private boolean validatePositions(GameMap map) {
        PositionArea area = new PositionArea(0, 0, X_SIZE, Y_SIZE);

        return map.getPositions().stream().allMatch(area::isInside);
    }

    private boolean validateTerrainDistribution(GameMap map) {
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

    private boolean validateBorderAccessibility(GameMap map) {
        PositionArea area = new PositionArea(0, 0, X_SIZE, Y_SIZE);

        long accessibleMapNodeCount = map.getMapNodes().stream()
                .filter(area::isOnBorder)
                .filter(GameMapNode::isAccessible)
                .count();

        return accessibleMapNodeCount >= BORDER_ACCESSIBLE_MIN_AMOUNT;
    }

    private boolean validateFortPlacement(GameMap map) {
        List<GameMapNode> fortNodes = map.getMapNodes().stream()
                .filter(GameMapNode::hasPlayerFort)
                .toList();

        if (fortNodes.size() != 1) {
            return false;
        }

        GameMapNode fortMapNode = fortNodes.getFirst();

        return !fortMapNode.hasOpponentFort()
                && fortMapNode.getTerrainType() == TerrainType.GRASS;
    }

    private boolean validateTreasurePlacement(GameMap map) {
        return map.getMapNodes().stream().noneMatch(GameMapNode::hasTreasure);
    }

    public boolean validate(GameMap map) {
        return validateSize(map)
                && validatePositions(map)
                && validateTerrainDistribution(map)
                && validateBorderAccessibility(map)
                && validateFortPlacement(map)
                && validateTreasurePlacement(map);
    }
}
