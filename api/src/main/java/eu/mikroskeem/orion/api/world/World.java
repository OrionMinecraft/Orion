package eu.mikroskeem.orion.api.world;

import org.bukkit.Location;

/**
 * @author Mark Vainomaa
 */
public interface World extends org.bukkit.World {
    /**
     * {@inheritDoc}
     * @deprecated Use {@link World#setSpawnLocation(Location)} or
     *             {@link World#setSpawnLocation(double, double, double, float, float)}
     */
    @Override
    @Deprecated
    default boolean setSpawnLocation(int x, int y, int z) {
        return setSpawnLocation(new Location(null, x, y, z, 0, 0));
    }

    /**
     * Set world spawn location
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param yaw Yaw
     * @param pitch Pitch
     * @return True, if spawn location was successfully set
     */
    default boolean setSpawnLocation(double x, double y, double z, float yaw, float pitch) {
        return setSpawnLocation(new Location(null, x, y, z, yaw, pitch));
    }

    /**
     * Set world spawn location
     * 
     * @param location Location object
     * @return True, if spawn location was successfully set
     * @see org.bukkit.World
     */
    boolean setSpawnLocation(Location location);
}
