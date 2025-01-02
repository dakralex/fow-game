package client.validation;

import client.map.GameMap;

public class HalfMapValidator {

    public boolean validate(GameMap map) {
        GameMapSizeValidator mapSizeValidator = new GameMapSizeValidator();
        GameMapPositionsValidator mapPositionsValidator = new GameMapPositionsValidator();
        GameMapTerrainDistributionValidator mapTerrainDistributionValidator = new GameMapTerrainDistributionValidator();
        GameMapBorderAccessibilityValidator mapBorderAccessibilityValidator = new GameMapBorderAccessibilityValidator();
        GameMapFortPlacementValidator mapFortPlacementValidator = new GameMapFortPlacementValidator();
        GameMapTreasurePlacementValidator mapTreasurePlacementValidator = new GameMapTreasurePlacementValidator();
        GameMapTerrainReachabilityValidator mapTerrainReachabilityValidator = new GameMapTerrainReachabilityValidator();

        return mapSizeValidator.validate(map)
                && mapPositionsValidator.validate(map)
                && mapTerrainDistributionValidator.validate(map)
                && mapBorderAccessibilityValidator.validate(map)
                && mapFortPlacementValidator.validate(map)
                && mapTreasurePlacementValidator.validate(map)
                && mapTerrainReachabilityValidator.validate(map);
    }
}
