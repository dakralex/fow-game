package client.validation;

import client.map.GameMap;

public interface GameMapValidationRule {

    public boolean validate(GameMap map);
}
