package eu.mikroskeem.orion.internal.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * @author Mark Vainomaa
 */
public class OrionCommand extends Command {
    public OrionCommand(String name) {
        super(name);
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        sender.sendMessage("Orion!");
        return true;
    }
}
