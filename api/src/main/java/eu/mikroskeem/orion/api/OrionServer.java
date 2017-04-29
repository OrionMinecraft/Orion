package eu.mikroskeem.orion.api;

import eu.mikroskeem.orion.api.server.Configuration;
import eu.mikroskeem.orion.api.world.World;

import java.util.List;

/**
 * @author Mark Vainomaa
 */
public interface OrionServer {
    Configuration getConfiguration();
    World getWorld(String worldName);
    List<World> getWorlds();
}
