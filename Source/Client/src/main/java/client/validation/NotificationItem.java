package client.validation;

public record NotificationItem<T>(T callee, String message) {
}
