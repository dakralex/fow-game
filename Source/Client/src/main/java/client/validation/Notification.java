package client.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Notification<T> {

    private final List<NotificationItem<T>> entries;

    public Notification() {
        this.entries = new ArrayList<>();
    }

    public void addEntry(T callee, String message) {
        entries.add(new NotificationItem<>(callee, message));
    }

    public boolean hasEntries() {
        return !entries.isEmpty();
    }

    @Override
    public String toString() {
        return entries.stream()
                .map(NotificationItem::message)
                .map(message -> String.format("- %s", message))
                .collect(Collectors.joining("\n"));
    }
}
