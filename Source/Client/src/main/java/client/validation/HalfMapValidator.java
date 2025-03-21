package client.validation;

import java.util.List;

import client.map.GameMap;

public class HalfMapValidator {

    private static final List<GameMapValidationRule> validationRules = List.of(
            new GameMapDimensionValidator(),
            new GameMapTerrainDistributionValidator(),
            new GameMapBorderAccessibilityValidator(),
            new GameMapFortPlacementValidator(),
            new GameMapTreasurePlacementValidator(),
            new GameMapTerrainReachabilityValidator()
    );

    public Notification<GameMapValidationRule> validate(GameMap map) {
        Notification<GameMapValidationRule> validationErrors = new Notification<>();

        validationRules.forEach(rule -> rule.validate(map, validationErrors));

        return validationErrors;
    }
}
