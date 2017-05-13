package eu.mikroskeem.orion.mod.mixins.spawnpoint;

import eu.mikroskeem.orion.internal.interfaces.OrionWorldData;
import net.minecraft.server.v1_11_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Mark Vainomaa
 */
@Mixin(value = EntityPlayer.class, remap = false)
public abstract class MixinEntityPlayer extends Entity {
    public MixinEntityPlayer() { super(null); }

    @Shadow public abstract CraftPlayer getBukkitEntity();

    private boolean orion$redirectWorldSpawn = false;

    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/v1_11_R1/WorldServer;" +
                    "getSpawn()Lnet/minecraft/server/v1_11_R1/BlockPosition;"
    ))
    public BlockPosition getSpawnProxy_ctor(WorldServer worldServer) {
        orion$redirectWorldSpawn = true;
        return worldServer.getSpawn();
    }

    @Redirect(method = "spawnIn", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/v1_11_R1/World;" +
                    "getSpawn()Lnet/minecraft/server/v1_11_R1/BlockPosition;"
    ))
    public BlockPosition getSpawnProxy_spawnIn(World world) {
        orion$redirectWorldSpawn = true;
        return world.getSpawn();
    }

    @Redirect(method = "spawnIn", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/v1_11_R1/EntityPlayer;setPosition(DDD)V"
    ))
    public void setPosition_spawnIn(EntityPlayer entityPlayer, double x, double y, double z) {
        if(orion$redirectWorldSpawn) {
            Location spawnpoint = ((OrionWorldData) world.worldData).getSpawnpoint();
            setPositionRotation(
                    spawnpoint.getX(),
                    spawnpoint.getY(),
                    spawnpoint.getZ(),
                    spawnpoint.getYaw(),
                    spawnpoint.getPitch()
            );
            orion$redirectWorldSpawn = false;
        }
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/v1_11_R1/EntityPlayer;" +
                    "setPositionRotation(Lnet/minecraft/server/v1_11_R1/BlockPosition;FF)V"
    ))
    public void setPositionRotation_ctor(EntityPlayer entity, BlockPosition blockposition, float yaw, float pitch) {
        if(orion$redirectWorldSpawn) {
            Location spawnpoint = ((OrionWorldData) world.worldData).getSpawnpoint();
            setPositionRotation(
                    spawnpoint.getX(),
                    spawnpoint.getY(),
                    spawnpoint.getZ(),
                    spawnpoint.getYaw(),
                    spawnpoint.getPitch()
            );
            orion$redirectWorldSpawn = false;
        }
    }
}
