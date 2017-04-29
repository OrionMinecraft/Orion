package eu.mikroskeem.orion.mod.mixins.spawnpoint;

import net.minecraft.server.v1_11_R1.EntityPlayer;
import net.minecraft.server.v1_11_R1.PlayerList;
import net.minecraft.server.v1_11_R1.WorldServer;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Mark Vainomaa
 */
@Mixin(PlayerList.class)
public abstract class MixinPlayerList {
    /* TODO: finish this
    private final Set<EntityPlayer> orion$redirectWorldSpawnForPlayers = new HashSet<>();

    @Inject(remap = false, method = "moveToWorld(Lnet/minecraft/server/v1_11_R1/EntityPlayer;IZ" +
            "Lorg/bukkit/Location;Z)" +
            "Lnet/minecraft/server/v1_11_R1/EntityPlayer;",
            at = @At(remap = false,
            shift = At.Shift.AFTER,
            value = "INVOKE",
            //target = "Lnet/minecraft/server/v1_11_R1/WorldServer;" +
            //        "getSpawn()Lnet/minecraft/server/v1_11_R1/BlockPosition;"
            //target = "Ljava/util/List;get(I)Ljava/lang/Object;"
            target = "Lorg/bukkit/Location;<init>(Lorg/bukkit/World;DDD)Lorg/bukkit/Location;"

    ))
    public void onGetWorldSpawn(EntityPlayer entityplayer, int i, boolean flag, Location location,
                                      boolean avoidSuffocation, CallbackInfoReturnable<EntityPlayer> cb) {
        new Throwable("PlayerList -> (after) new Location()").printStackTrace();
        //orion$redirectWorldSpawnForPlayers.add(entityplayer);
    }*/
}
