package eu.mikroskeem.orion.launcher.mixins;

import eu.mikroskeem.orion.OrionServerCore;
import eu.mikroskeem.orion.api.Orion;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Mark Vainomaa
 */
@Mixin(Bukkit.class)
public abstract class MixinBukkit {
    @Inject(remap = false, method = "setServer(Lorg/bukkit/Server;)V", at = @At(value = "HEAD", remap = false))
    private static void onSetServer(Server server, CallbackInfo callbackInfo){
        Orion.setServer(new OrionServerCore());
        server.getLogger().info("This server is launched with OrionLauncher, made with \u2764 by mikroskeem");
    }
}
