package eu.mikroskeem.orion.api;

import eu.mikroskeem.orion.api.server.Configuration;
import eu.mikroskeem.orion.api.server.SentryReporter;
import eu.mikroskeem.orion.api.world.World;

import java.util.List;

/**
 * Orion server core API
 *
 * @author Mark Vainomaa
 */
public interface OrionServer {
    /**
     * Get server configuration
     *
     * @return Instance of {@link Configuration}
     */
    Configuration getConfiguration();

    /**
     * Get world with name.
     *
     * @param worldName World name
     * @return {@link World} or null
     */
    World getWorld(String worldName);

    /**
     * List all available and loaded worlds
     *
     * @return List of {@link World}s
     */
    List<World> getWorlds();

    /**
     * Get Sentry reporter
     *
     * @return Instance of {@link SentryReporter}
     */
    SentryReporter getSentryReporter();
}
