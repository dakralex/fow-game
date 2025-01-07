package client.util;

public interface Observer<E> {

    /**
     * Notifies the observer to update with respect to the given {@code event}.
     *
     * @param event triggering event
     */
    void update(E event);
}
