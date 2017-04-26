package eu.mikroskeem.orion.api.events.player.idle;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * This event gets fired when player goes idle
 * @author Mark Vainomaa
 */
public final class PlayerIdleEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    private final long timestamp;

    public PlayerIdleEvent(Player player, long timestamp) {
        super(player);
        this.timestamp = timestamp;
    }

    /**
     * Get timestamp of time when player was marked idle
     * @return Seconds from Unix time epoch
     */
    public long getTimestamp() {
        return timestamp;
    }

    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
