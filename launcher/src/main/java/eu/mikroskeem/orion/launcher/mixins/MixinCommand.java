package eu.mikroskeem.orion.launcher.mixins;

import eu.mikroskeem.orion.impl.configuration.StaticConfiguration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * @author Mark Vainomaa
 */
@Mixin(Command.class)
public abstract class MixinCommand {
    @Shadow(remap = false) private String permission;
    @Shadow(remap = false) private String name;
    @Shadow(remap = false) private String permissionMessage;
    @Shadow(remap = false) public abstract boolean testPermissionSilent(CommandSender target);

    public boolean testPermission(CommandSender target) {
        if (!testPermissionSilent(target)) {
            String[] messages = (permissionMessage != null? permissionMessage :
                    StaticConfiguration.COMMAND_PERMISSION_DENIED_MESSAGE)
                    .replaceAll("<permission>", permission) // Retain legacy placeholder
                    .replaceAll("%permission%", permission)
                    .replaceAll("%command%", name)
                    .split("\n");
            target.sendMessage(messages);
            return false;
        }
        return true;
    }
}
