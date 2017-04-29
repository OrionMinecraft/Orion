package eu.mikroskeem.orion.internal.interfaces;

import org.bukkit.Location;

/**
 * @author Mark Vainomaa
 */
public interface OrionWorldData {
    double getSpawnpointX();
    double getSpawnpointY();
    double getSpawnpointZ();
    float getSpawnpointYaw();
    float getSpawnpointPitch();

    void setSpawnpointX(double value);
    void setSpawnpointY(double value);
    void setSpawnpointZ(double value);
    void setSpawnpointYaw(float value);
    void setSpawnpointPitch(float value);

    Location getSpawnpoint();
    void setSpawnpoint(Location location);
}
