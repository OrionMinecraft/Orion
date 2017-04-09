package eu.mikroskeem.orion.launcher.mixins;

import eu.mikroskeem.orion.api.Orion;
import eu.mikroskeem.orion.api.server.Configuration;
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
            Configuration configuration = Orion.getServer().getConfiguration();

            String[] messages = (permissionMessage != null? permissionMessage :
                    configuration.getMessages().getCommandPermissionDeniedMessage())
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
