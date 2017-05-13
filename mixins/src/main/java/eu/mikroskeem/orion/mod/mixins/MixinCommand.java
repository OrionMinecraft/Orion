package eu.mikroskeem.orion.mod.mixins;

import eu.mikroskeem.orion.api.Orion;
import eu.mikroskeem.orion.api.server.Configuration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * @author Mark Vainomaa
 */
@Mixin(value = Command.class, remap = false)
public abstract class MixinCommand {
    @Shadow private String permission;
    @Shadow private String name;
    @Shadow private String permissionMessage;
    @Shadow public abstract boolean testPermissionSilent(CommandSender target);

    public boolean testPermission(CommandSender target) {
        if (!testPermissionSilent(target)) {
            Configuration configuration = Orion.getServer().getConfiguration();
            String globalPermissionDeniedMessage = configuration.getMessages().getCommandPermissionDeniedMessage();

            String[] messages = (configuration.getCommands().isOverridingPluginCommandPermissionDeniedMessageEnabled()?
                    (permissionMessage != null? permissionMessage : globalPermissionDeniedMessage):
                    globalPermissionDeniedMessage)
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
