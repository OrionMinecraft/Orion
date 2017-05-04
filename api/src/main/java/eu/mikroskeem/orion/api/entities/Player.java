package eu.mikroskeem.orion.api.entities;

import eu.mikroskeem.orion.api.server.Configuration;

/**
 * @author Mark Vainomaa
 */
public interface Player extends org.bukkit.entity.Player {
    /**
     * Get the last time when player was active
     *
     * @return Seconds from Unix epoch
     */
    long getLastActiveTime();

    /**
     * Shows if player is away or not, value is set by comparing
     * value in {@link Configuration.PlayerConfiguration#getMillisecondsUntilToMarkPlayerAway()}
     *
     * @return Whether player is away or not
     */
    boolean isAway();
}
