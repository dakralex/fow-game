package client.validation;

import java.util.List;

import client.map.GameMap;

public class HalfMapValidator {

    private final List<GameMapValidationRule> validationRules = List.of(
            new GameMapSizeValidator(),
            new GameMapPositionsValidator(),
            new GameMapTerrainDistributionValidator(),
            new GameMapBorderAccessibilityValidator(),
            new GameMapFortPlacementValidator(),
            new GameMapTreasurePlacementValidator(),
            new GameMapTerrainReachabilityValidator()
    );

    public boolean validate(GameMap map) {
        return validationRules.stream()
                .allMatch(rule -> rule.validate(map));
    }
}
