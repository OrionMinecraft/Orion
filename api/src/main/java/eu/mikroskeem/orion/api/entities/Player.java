package eu.mikroskeem.orion.api.entities;

/**
 * @author Mark Vainomaa
 */
public interface Player extends org.bukkit.entity.Player {
    long getLastActiveTime();
    boolean isAway();
}
