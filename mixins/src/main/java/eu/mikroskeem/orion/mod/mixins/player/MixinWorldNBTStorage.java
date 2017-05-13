package eu.mikroskeem.orion.mod.mixins.player;

import eu.mikroskeem.orion.api.Orion;
import net.minecraft.server.v1_11_R1.EntityHuman;
import net.minecraft.server.v1_11_R1.IPlayerFileData;
import net.minecraft.server.v1_11_R1.NBTTagCompound;
import net.minecraft.server.v1_11_R1.WorldNBTStorage;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Mark Vainomaa
 */
@Mixin(value = WorldNBTStorage.class, remap = false)
@Implements(@Interface(iface = IPlayerFileData.class, prefix = "pfd$"))
public abstract class MixinWorldNBTStorage {
    @Shadow public abstract String[] getSeenPlayers();

    @Inject(method = "save", cancellable = true, at = @At("HEAD"))
    public void onSave(EntityHuman e, CallbackInfo cb) {
        if(Orion.getServer().getConfiguration().getPlayerConfiguration().isPlayerDataSavingDisabled()) {
            cb.cancel();
        }
    }

    @Inject(method = "load", cancellable = true, at = @At("HEAD"))
    public void load(EntityHuman e, CallbackInfoReturnable<NBTTagCompound> cb) {
        if(Orion.getServer().getConfiguration().getPlayerConfiguration().isPlayerDataSavingDisabled()) {
            cb.setReturnValue(null);
        }
    }

    @Intrinsic(displace = true)
    public String[] pfd$getSeenPlayers() {
        if(Orion.getServer().getConfiguration().getPlayerConfiguration().isPlayerDataSavingDisabled()) {
            return new String[0];
        }
        return this.getSeenPlayers();
    }
}
