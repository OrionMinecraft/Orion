package eu.mikroskeem.orion.api.events;

import java.util.function.Consumer;

/**
 * @author Mark Vainomaa
 */
public interface EventBus {
    <T extends Event> T fire(T event);
    <T extends Event> void register(Class<T> eventClass, Consumer<T> listener);
    <T extends Event> void unregister(Class<T> eventClass, Consumer<T> listener);
    <T extends Event> void unregisterAll(Class<T> eventClass);
}
