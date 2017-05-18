package eu.mikroskeem.orion.mod.mixins.spawnpoint;

import eu.mikroskeem.orion.internal.interfaces.OrionWorldData;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_11_R1.*;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Mark Vainomaa
 */
@Mixin(value = WorldData.class, remap = false)
public abstract class MixinWorldData implements OrionWorldData {
    @Shadow public WorldServer world;
    @Shadow public abstract int b(); // MCP - getSpawnX
    @Shadow public abstract int c(); // MCP - getSpawnY
    @Shadow public abstract int d(); // MCP - getSpawnZ
    @Shadow private int h;  // MCP - spawnX
    @Shadow private int j;  // MCP - spawnY
    @Shadow private int i;  // MCP - spawnZ

    @Getter @Setter private double spawnpointX;
    @Getter @Setter private double spawnpointY;
    @Getter @Setter private double spawnpointZ;
    @Getter @Setter private float spawnpointYaw;
    @Getter @Setter private float spawnpointPitch;

    @Override
    public Location getSpawnpoint() {
        return new Location(
                this.world.getWorld(),
                getSpawnpointX(),
                getSpawnpointY(),
                getSpawnpointZ(),
                getSpawnpointYaw(),
                getSpawnpointPitch()
        );
    }

    @Override
    public void setSpawnpoint(Location location) {
        setSpawnpointX(location.getX());
        setSpawnpointY(location.getY());
        setSpawnpointZ(location.getZ());
        setSpawnpointYaw(location.getYaw());
        setSpawnpointPitch(location.getPitch());

        /* Sigh... */
        this.h = location.getBlockX();
        this.i = location.getBlockY();
        this.j = location.getBlockZ();
    }

    /* updateTagCompound - in MCP */
    @Inject(method = "a(Lnet/minecraft/server/v1_11_R1/NBTTagCompound;" +
            "Lnet/minecraft/server/v1_11_R1/NBTTagCompound;)V", at = @At("HEAD"))
    public void onUpdateTagCompound(NBTTagCompound nbt, NBTTagCompound playerNbt, CallbackInfo cb) {
        NBTTagCompound orionSpawn = new NBTTagCompound();
        orionSpawn.setDouble("x", spawnpointX);
        orionSpawn.setDouble("y", spawnpointY);
        orionSpawn.setDouble("z", spawnpointZ);
        orionSpawn.setFloat("yaw", spawnpointYaw);
        orionSpawn.setFloat("pitch", spawnpointPitch);
        nbt.set("orion.spawnpoint", orionSpawn);
    }

    @Inject(method = "<init>(Lnet/minecraft/server/v1_11_R1/NBTTagCompound;)V", at = @At("RETURN"))
    public void onConstructUsingNBT(NBTTagCompound nbt, CallbackInfo cb) {
        this.spawnpointX = this.b();
        this.spawnpointY = this.c();
        this.spawnpointZ = this.d();
        this.spawnpointYaw = 0;
        this.spawnpointPitch = 0;
        if(nbt.hasKeyOfType("orion.spawnpoint", 10)) {
            NBTTagCompound spawnpoint = nbt.getCompound("orion.spawnpoint");
            this.spawnpointX = spawnpoint.getDouble("x");
            this.spawnpointY = spawnpoint.getDouble("y");
            this.spawnpointZ = spawnpoint.getDouble("z");
            this.spawnpointYaw = spawnpoint.getFloat("yaw");
            this.spawnpointPitch = spawnpoint.getFloat("pitch");
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/server/v1_11_R1/WorldData;)V", at = @At("RETURN"))
    public void onConstructUsingOtherWorldData(WorldData worldData, CallbackInfo cb) {
        OrionWorldData orionWorldData = ((OrionWorldData) worldData);
        this.spawnpointX = orionWorldData.getSpawnpointX();
        this.spawnpointY = orionWorldData.getSpawnpointY();
        this.spawnpointZ = orionWorldData.getSpawnpointZ();
        this.spawnpointYaw = orionWorldData.getSpawnpointYaw();
        this.spawnpointPitch = orionWorldData.getSpawnpointPitch();
    }

    /* Cancel spawn setting from internal NMS code */
    @Inject(method = "setSpawn", cancellable = true, at = @At("HEAD"))
    public void onSetSpawn(BlockPosition blockPosition, CallbackInfo cb) {
        new Throwable("WorldData -> setSpawn(). Please report this to @mikroskeem").printStackTrace();
        cb.cancel();
    }
}
