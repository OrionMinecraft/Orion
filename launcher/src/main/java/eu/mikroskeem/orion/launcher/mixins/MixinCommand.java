package eu.mikroskeem.orion.launcher.mixins;

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
        if (testPermissionSilent(target)) {
            return true;
        }

        if (permissionMessage == null) {
            target.sendMessage("no command " + name + " for you");
        } else if (permissionMessage.length() != 0) {
            for (String line : permissionMessage.replace("<permission>", permission).split("\n")) {
                target.sendMessage(line);
            }
        }

        return false;
    }
}
