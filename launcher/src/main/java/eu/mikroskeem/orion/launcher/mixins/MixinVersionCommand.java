package eu.mikroskeem.orion.launcher.mixins;

import com.google.common.collect.ImmutableList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.VersionCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Mark Vainomaa
 */
@Mixin(VersionCommand.class)
public abstract class MixinVersionCommand extends Command {
    public MixinVersionCommand(){super(null, null, null, ImmutableList.of()); }

    @Shadow(remap = false) protected abstract void sendVersion(CommandSender sender);

    @Redirect(remap = false,
            method = "execute(Lorg/bukkit/command/CommandSender;Ljava/lang/String;[Ljava/lang/String;)Z",
            at = @At(remap = false, value = "INVOKE",
            target = "Lorg/bukkit/command/defaults/VersionCommand;sendVersion(Lorg/bukkit/command/CommandSender;)V"
    ))
    public void proxySendVersion(VersionCommand versionCommand, CommandSender sender) {
        sendVersion(sender);
        sender.sendMessage("§7Server uses §bOrion Launcher §7made with §c\u2764 §7by §c§lmikroskeem");
    }
}
