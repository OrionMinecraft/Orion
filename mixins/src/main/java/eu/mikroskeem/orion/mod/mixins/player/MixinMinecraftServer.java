package eu.mikroskeem.orion.mod.mixins.player;

import eu.mikroskeem.orion.api.Orion;
import eu.mikroskeem.orion.api.server.Configuration;
import eu.mikroskeem.orion.mod.impl.NullPlayerNBTStorage;
import net.minecraft.server.v1_11_R1.MinecraftServer;
import net.minecraft.server.v1_11_R1.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Mark Vainomaa
 */
@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {
    @Shadow(remap = false) private PlayerList v;

    /* loadAllWorlds - in MCP */
    @Inject(remap = false, method = "a(Ljava/lang/String;Ljava/lang/String;J" +
            "Lnet/minecraft/server/v1_11_R1/WorldType;Ljava/lang/String;)V", at = @At(remap = false,
            value = "TAIL"
    ))
    public void onLoadAllWorlds(CallbackInfo cb) {
        Configuration configuration = Orion.getServer().getConfiguration();
        if(configuration.getPlayerConfiguration().isPlayerDataSavingDisabled()) {
            v.playerFileData = new NullPlayerNBTStorage();
        }
    }
}
