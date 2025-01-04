package client.validation.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import client.map.GameMap;
import client.validation.GameMapValidationRule;
import client.validation.Notification;

public class NotificationAssertUtils {

    public static void assertSomeViolation(GameMap map,
                                           GameMapValidationRule validator) {
        Notification<GameMapValidationRule> validationErrors = new Notification<>();

        validator.validate(map, validationErrors);

        assertTrue(validationErrors.hasEntries(),
                   "Validator should have caught at least one business rule violation");

    }

    public static void assertNoViolation(GameMap map,
                                         GameMapValidationRule validator) {
        Notification<GameMapValidationRule> validationErrors = new Notification<>();

        validator.validate(map, validationErrors);

        assertFalse(validationErrors.hasEntries(),
                    "Validator should have caught no business rule violation");

    }
}
