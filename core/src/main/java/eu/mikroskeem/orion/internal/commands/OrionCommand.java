package eu.mikroskeem.orion.internal.commands;

import eu.mikroskeem.orion.api.Orion;
import eu.mikroskeem.orion.api.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

/**
 * @author Mark Vainomaa
 */
@Slf4j
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
        if(!testPermission(sender)) return true;
        String[] message;
        if(args.length > 0) {
            switch (args[0]){
                case "debug":
                    if(args.length == 1) {
                        message = new String[]{
                            "§8[§b§lOrion§8]§7 Server debug utilities:",
                            "§8- §7Show server/VM information: §c/orion debug info",
                            "§8- §7Create listener: §c/orion debug listen §8[§blistenername§8] [§beventname§8] §8[§bgroovyscript§8]"
                        };
                        sender.sendMessage(message);
                    } else {
                        switch (args[1]){
                            case "info":
                                sender.sendMessage(getInfo());
                                break;
                            case "listen":
                                sender.sendMessage("§8[§b§lOrion§8]§7 §oWork in progress...");
                                break;
                            case "removelistener":
                                sender.sendMessage("§8[§b§lOrion§8]§7 §oWork in progress...");
                                break;
                            default:
                                sender.sendMessage(String.format("§8[§b§lOrion§8]§7 Unknown subcommand: '§c%s§7'", args[0]));
                                break;
                        }
                    }
                    break;
                case "reload":
                    try {
                        Orion.getServer().getConfiguration().reload();
                        sender.sendMessage("§8[§b§lOrion§8]§a Configuration reloaded!");
                    } catch (IOException e){
                        sender.sendMessage("§8[§b§lOrion§8]§c Failed to reload configuration. Look into console for errors.");
                        log.error("Failed to reload Orion configuration. See stacktrace below.");
                        e.printStackTrace();
                    }
                    break;
                case "help":
                    sender.sendMessage("§8[§b§lOrion§8]§7 §oWork in progress...");
                    break;
                default:
                    sender.sendMessage(String.format("§8[§b§lOrion§8]§7 Unknown subcommand: '§c%s§7'", args[0]));
                    break;
            }
        } else {
            message = new String[]{
                "§8[§b§lOrion§8]§7 Orion Server Core",
                "§7See §c/orion help§7 for more information"
            };
            sender.sendMessage(message);
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        return super.tabComplete(sender, alias, args);
    }

    private String[] getInfo(){
        Runtime runtime = Runtime.getRuntime();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        String javaVersion = Runtime.class.getPackage().getImplementationVersion();

        /* Memory usage */
        long maxMemory = Math.round(runtime.maxMemory() / 1024 / 1024);
        long allocatedMemory = Math.round(runtime.totalMemory() / 1024 / 1024);
        long freeMemory = Math.round(runtime.freeMemory() / 1024 / 1024);

        /* Uptime */
        long uptime = runtimeBean.getStartTime();

        return new String[]{
                "§8[§b§lOrion§8]§7 Server and VM info",
                "§7Java version: §c" + javaVersion,
                "§7Uptime: §c" + DateUtil.formatDateDiff(uptime),
                "§7TPS: §f" + formatTps(Bukkit.getTPS()),
                "§7Maximum memory: §c" + Math.round(maxMemory) + "§7MB",
                "§7Allocated memory: §c" + Math.round(allocatedMemory) + "§7MB",
                "§7Free memory: §c" + Math.round(freeMemory) + "§7MB",
        };
    }

    private static String formatTps(double[] tps) {
        return formatTps(tps[0]) + " " + formatTps(tps[1]) + " " + formatTps(tps[2]);
    }

    /* Source: org.spigotmc.TicksPerSecondCommand */
    private static String formatTps(double tps) {
        return ((tps > 18.0)?ChatColor.GREEN:(tps > 16.0)?ChatColor.YELLOW:ChatColor.RED).toString()
                + ((tps > 20.0)?"*":"")+Math.min(Math.round(tps*100.0)/100.0, 20.0);
    }
}
