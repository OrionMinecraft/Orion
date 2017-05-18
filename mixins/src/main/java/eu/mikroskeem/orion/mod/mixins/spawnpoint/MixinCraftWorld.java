package eu.mikroskeem.orion.mod.mixins.spawnpoint;

import eu.mikroskeem.orion.api.world.World;
import eu.mikroskeem.orion.internal.interfaces.OrionWorldData;
import net.minecraft.server.v1_11_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * @author Mark Vainomaa
 */
@Mixin(value = CraftWorld.class, remap = false)
public abstract class MixinCraftWorld implements World {
    @Shadow @Final private WorldServer world;

    @Override
    public Location getSpawnLocation() {
        OrionWorldData worldData = (OrionWorldData) this.world.worldData;
        return worldData.getSpawnpoint();
    }

    @Override
    public boolean setSpawnLocation(Location location) {
        OrionWorldData worldData = (OrionWorldData) this.world.worldData;
        worldData.setSpawnpoint(location);
        return true;
    }

    @Override
    public boolean setSpawnLocation(int x, int y, int z) {
        return setSpawnLocation(new Location(null, x, y, z, 0, 0));
    }

    @Override
    public String toString() {
        return "OrionWorld{name=" + this.getName() + '}';
    }
}
