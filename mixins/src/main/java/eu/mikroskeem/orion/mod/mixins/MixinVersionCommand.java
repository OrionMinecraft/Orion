package eu.mikroskeem.orion.mod.mixins;

import eu.mikroskeem.orion.api.Orion;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.VersionCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Mark Vainomaa
 */
@Mixin(value = VersionCommand.class, remap = false)
public abstract class MixinVersionCommand {
    @Shadow protected abstract void sendVersion(CommandSender sender);

    @Redirect(method = "execute(Lorg/bukkit/command/CommandSender;Ljava/lang/String;[Ljava/lang/String;)Z",
            at = @At(value = "INVOKE",
            target = "Lorg/bukkit/command/defaults/VersionCommand;sendVersion(Lorg/bukkit/command/CommandSender;)V"
    ))
    public void proxySendVersion(VersionCommand versionCommand, CommandSender sender) {
        sendVersion(sender);
        sender.sendMessage("§7Server uses §bOrion Launcher §7(version §c"+ Orion.getVersion()+"§7) "+
                "made with §c\u2764 §7by §c§lmikroskeem");
    }
}
