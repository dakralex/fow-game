package client.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A generic observable, which emits events of type {@code E}, which {@link Observer<E>} can
 * react to by subscribing to the observable.
 *
 * @param <E> event type
 */
public class Observable<E> {

    private final Map<Observer<E>, Set<E>> observers = HashMap.newHashMap(1);

    private Set<E> getEventsFor(Observer<E> observer) {
        return observers.getOrDefault(observer, LinkedHashSet.newLinkedHashSet(1));
    }

    /**
     * Subscribe to the observable to receive updates about specific events.
     *
     * @param observer the subscribing observer
     * @param newEvents events to subscribe to
     */
    public void subscribe(Observer<E> observer, Collection<E> newEvents) {
        Set<E> events = getEventsFor(observer);

        events.addAll(newEvents);

        observers.put(observer, events);
    }

    /**
     * Unsubscribe from the observable to not receive updates about specific events anymore.
     *
     * @param observer the unsubscribing observer
     * @param oldEvents events to unsubscribe from
     */
    public void unsubscribe(Observer<E> observer, Collection<E> oldEvents) {
        Set<E> events = getEventsFor(observer);

        events.removeAll(oldEvents);

        observers.put(observer, events);
    }

    private Predicate<Observer<E>> notifiableFor(E event) {
        return observer -> getEventsFor(observer).contains(event);
    }

    protected void notifyObservers(E event) {
        Set<Observer<E>> notifiableObservers = observers.keySet().stream()
                .filter(notifiableFor(event))
                .collect(Collectors.toSet());

        notifiableObservers.forEach(observer -> observer.update(event));
    }
}
