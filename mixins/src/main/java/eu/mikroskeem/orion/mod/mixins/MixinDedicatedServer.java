package eu.mikroskeem.orion.mod.mixins;

import eu.mikroskeem.orion.api.events.server.ServerStartedEvent;
import net.minecraft.server.v1_11_R1.DedicatedServer;
import org.bukkit.Bukkit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Mark Vainomaa
 */
@Mixin(DedicatedServer.class)
public abstract class MixinDedicatedServer {
    @Inject(remap = false, method = "init", at = @At(remap = false, value = "TAIL"))
    public void onDone(CallbackInfoReturnable<Boolean> cb) {
        Bukkit.getPluginManager().callEvent(new ServerStartedEvent());
    }
}
