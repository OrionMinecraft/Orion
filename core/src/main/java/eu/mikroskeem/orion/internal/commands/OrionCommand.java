package eu.mikroskeem.orion.internal.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * @author Mark Vainomaa
 */
public class OrionCommand extends Command {
    /*
    /orion debug listen shittychattest AsyncPlayerChatEvent player.sendMessage "chat evt: ${event.getMessage()}"
     */

    public OrionCommand(String name) {
        super(name);
        this.description = "Orion Core commands";
        this.usageMessage = "/orion [options]";
        this.setPermission("orion.command.orion");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        sender.sendMessage("Orion!");
        return true;
    }
}
