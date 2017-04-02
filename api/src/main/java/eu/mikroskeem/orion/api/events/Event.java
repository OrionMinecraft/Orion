package eu.mikroskeem.orion.api.events;

/**
 * @author Mark Vainomaa
 */
public interface Event {
    boolean setAsync(boolean value);
    boolean isAsync();
}
