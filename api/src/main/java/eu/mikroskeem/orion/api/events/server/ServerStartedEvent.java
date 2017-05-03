package eu.mikroskeem.orion.api.events.server;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event gets fired when server is fully initialized
 *
 * @author Mark Vainomaa
 */
public final class ServerStartedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public ServerStartedEvent() { super(); }
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
