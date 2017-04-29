package eu.mikroskeem.orion.internal.commands;

import com.google.common.collect.ImmutableList;
import eu.mikroskeem.orion.api.Orion;
import eu.mikroskeem.orion.api.world.World;
import eu.mikroskeem.orion.internal.interfaces.OrionVanillaCommand;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Mark Vainomaa
 */
public class SetWorldSpawnCommand extends Command implements OrionVanillaCommand {
    public SetWorldSpawnCommand(String name) {
        super(name);
        this.description = "Sets a worlds's spawn point. If no coordinates are specified, the player's coordinates will be used.";
        this.usageMessage = "/setworldspawn OR /setworldspawn <x> <y> <z> <yaw> <pitch>";
        this.setPermission("orion.command.setworldspawn");
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        } else {
            Player player = null;
            World world;
            if (sender instanceof Player) {
                player = (Player) sender;
                world = (World)player.getWorld();
            } else {
                world = Orion.getServer().getWorlds().get(0);
            }
            Location newSpawnLocation;
            if (args.length == 0) {
                if (player == null) {
                    sender.sendMessage("You can only perform this command as a player");
                    return true;
                }
                newSpawnLocation = player.getLocation().clone();
            } else {
                if (args.length != 5) {
                    sender.sendMessage(ChatColor.RED + "Usage: " + this.usageMessage);
                    return false;
                }

                try {
                    double x = this.getDouble(sender, args[0], -30000000, 30000000, true);
                    double y = this.getDouble(sender, args[1], 0, world.getMaxHeight(), true);
                    double z = this.getDouble(sender, args[2], -30000000, 30000000, true);
                    float yaw = this.getFloat(sender, args[3], -180, 180, true);
                    float pitch = this.getFloat(sender, args[3], -90, 90, true);
                    newSpawnLocation = new Location(world, x, y, z, yaw, pitch);
                } catch (NumberFormatException var10) {
                    sender.sendMessage(var10.getMessage());
                    return true;
                }
            }

            world.setSpawnLocation(newSpawnLocation);
            Command.broadcastCommandMessage(sender, String.format(
                    "Set world %s's spawnpoint to (X: %.2f, Y: %.2f, Z: %.2f, Yaw: %.2f, Pitch: %.2f)",
                    world.getName(),
                    newSpawnLocation.getX(),
                    newSpawnLocation.getY(),
                    newSpawnLocation.getZ(),
                    newSpawnLocation.getYaw(),
                    newSpawnLocation.getPitch()
            ));
            return true;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        return ImmutableList.of();
    }
}
