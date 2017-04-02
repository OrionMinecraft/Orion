package eu.mikroskeem.orion.launcher.mixins;

import org.bukkit.command.Command;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * @author Mark Vainomaa
 */
@Mixin(Command.class)
public class MixinCommand {
    @Shadow(remap = false) private String name;

    public String getPermissionMessage(){
        return "No command " + this.name + "for you.";
    }
}
