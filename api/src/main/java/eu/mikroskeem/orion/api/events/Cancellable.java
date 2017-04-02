package eu.mikroskeem.orion.api.events;

/**
 * @author Mark Vainomaa
 */
public interface Cancellable {
    boolean isCancelled();
    boolean setCancelled(boolean cancelled);
}
