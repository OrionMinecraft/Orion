package eu.mikroskeem.orion.launcher.mixins;

import eu.mikroskeem.orion.OrionServerCore;
import eu.mikroskeem.orion.api.Orion;
import eu.mikroskeem.orion.api.OrionServer;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.logging.Level;

/**
 * @author Mark Vainomaa
 */
@Mixin(Bukkit.class)
public abstract class MixinBukkit {
    @Inject(remap = false, method = "setServer(Lorg/bukkit/Server;)V", at = @At(value = "HEAD", remap = false))
    private static void onSetServer(Server server, CallbackInfo callbackInfo){
        OrionServer orionServer = new OrionServerCore();
        Orion.setServer(orionServer);
        try {
            orionServer.getConfiguration().load();
            orionServer.getConfiguration().save();
        } catch (IOException e){
            server.getLogger().log(Level.SEVERE, "Failed to load Orion configuration!", e);

        }
        server.getLogger().info("This server is launched with OrionLauncher, made with \u2764 by mikroskeem");
    }
}
