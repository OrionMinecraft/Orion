package eu.mikroskeem.orion.api.events.player.chat;

import eu.mikroskeem.orion.api.entities.Player;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * This event gets fired when ${@link Player#sendMessage(String)} and friends is invoked
 *
 * @author Mark Vainomaa
 */
public final class PlayerPluginSendMessageEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    @Getter @Setter private boolean cancelled = false;
    private String message;
    public PlayerPluginSendMessageEvent(Player player, String message) {
        super(player);
        this.message = message;
    }

    /**
     * Get message what player will receive
     *
     * @return Message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set message what player will receive
     *
     * @param message Message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
