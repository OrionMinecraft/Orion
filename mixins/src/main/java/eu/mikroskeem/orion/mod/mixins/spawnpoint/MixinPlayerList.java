package eu.mikroskeem.orion.mod.mixins.spawnpoint;

import eu.mikroskeem.orion.internal.interfaces.OrionWorldData;
import net.minecraft.server.v1_11_R1.PlayerList;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Mark Vainomaa
 */
@Mixin(PlayerList.class)
public abstract class MixinPlayerList {
    private final static String MOVE_TO_WORLD = "moveToWorld(Lnet/minecraft/server/v1_11_R1/EntityPlayer;IZ" +
            "Lorg/bukkit/Location;Z)Lnet/minecraft/server/v1_11_R1/EntityPlayer;";

    @Redirect(remap = false, method = MOVE_TO_WORLD,
            at = @At(remap = false,
            value = "NEW",
            target = "(Lorg/bukkit/World;DDD)Lorg/bukkit/Location;",
            ordinal = 1,
            args = "class=org/bukkit/Location"
    ))
    public Location newLocation(World world, double x, double y, double z) {
        return ((OrionWorldData) ((CraftWorld) world).getHandle().worldData).getSpawnpoint();
    }
}
