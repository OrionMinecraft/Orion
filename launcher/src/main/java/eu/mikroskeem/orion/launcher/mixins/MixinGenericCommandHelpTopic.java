package eu.mikroskeem.orion.launcher.mixins;

import org.bukkit.command.CommandSender;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpTopic;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author Mark Vainomaa
 */
@Mixin(value = GenericCommandHelpTopic.class, remap = false)
public abstract class MixinGenericCommandHelpTopic extends HelpTopic {
    public void amendTopic(String amendedShortText, String amendedFullText){
        /* No-op */
    }

    public String getFullText(CommandSender forWho) {
        /* No-op */
        return "";
    }

    public boolean canSee(CommandSender sender){
        /* No-op */
        return false;
    }
}
