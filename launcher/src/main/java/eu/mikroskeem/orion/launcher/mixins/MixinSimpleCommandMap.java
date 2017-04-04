package eu.mikroskeem.orion.launcher.mixins;

import eu.mikroskeem.orion.internal.commands.OrionCommand;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Mark Vainomaa
 */
@Mixin(SimpleCommandMap.class)
public abstract class MixinSimpleCommandMap {
    @Shadow(remap = false) public abstract boolean register(String fallbackPrefix, Command command);

    @Inject(remap = false, method = "setDefaultCommands()V", at = @At(remap = false, value = "HEAD"))
    public void onSetDefaultCommands(CallbackInfo callbackInfo){
        register("orion", new OrionCommand("orion"));
    }
}
