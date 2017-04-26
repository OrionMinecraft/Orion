package eu.mikroskeem.orion.mod.mixins;

import eu.mikroskeem.orion.api.entities.Player;
import eu.mikroskeem.orion.api.events.player.idle.PlayerActiveEvent;
import eu.mikroskeem.orion.api.events.player.idle.PlayerIdleEvent;
import net.minecraft.server.v1_11_R1.MinecraftServer;
import net.minecraft.server.v1_11_R1.PlayerConnection;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Mark Vainomaa
 */
@Mixin(PlayerConnection.class)
public abstract class MixinPlayerConnection {
    @Shadow(remap = false) public abstract CraftPlayer getPlayer();

    private int orion$awayTicks = 0;

    @Inject(remap = false, method = "F_", at = @At(remap = false, value = "TAIL"))
    public void onUpdate(CallbackInfo cb) {
        Player player = (Player) getPlayer();
        if(player.isAway()) {
            if(orion$awayTicks == 0) {
                getPlayer().getServer().getPluginManager()
                        .callEvent(new PlayerIdleEvent(player, MinecraftServer.aw()));
            }
            orion$awayTicks++;
        } else {
            if(orion$awayTicks > 0) {
                orion$awayTicks = 0;
                getPlayer().getServer().getPluginManager()
                        .callEvent(new PlayerActiveEvent(player, MinecraftServer.aw()));
            }
        }
    }
}
