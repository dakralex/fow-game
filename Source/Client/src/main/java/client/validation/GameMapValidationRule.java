package client.validation;

import client.map.GameMap;

public interface GameMapValidationRule {

    void validate(GameMap map, Notification<GameMapValidationRule> note);
}
