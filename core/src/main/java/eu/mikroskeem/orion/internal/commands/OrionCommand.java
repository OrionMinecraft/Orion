package eu.mikroskeem.orion.internal.commands;

import com.google.common.collect.ImmutableList;
import eu.mikroskeem.orion.api.Orion;
import eu.mikroskeem.orion.api.events.Event;
import eu.mikroskeem.orion.api.utils.DateUtil;
import eu.mikroskeem.orion.internal.debug.ClassCache;
import eu.mikroskeem.orion.internal.debug.DebugListenerManager;
import eu.mikroskeem.orion.internal.debug.PasteUtility;
import eu.mikroskeem.shuriken.common.Ensure;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.codehaus.groovy.control.CompilationFailedException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Mark Vainomaa
 */
@Slf4j
public class OrionCommand extends Command {
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
                            "§8- §7Create listener: §c/orion debug addlistener §8[§blistenername§8] [§beventname§8] §8[§bgroovyscript§8]"
                        };
                        sender.sendMessage(message);
                    } else {
                        switch (args[1]){
                            case "info":
                                sender.sendMessage(getInfo());
                                break;
                            case "addlistener":
                                if(args.length >= 5) {
                                    String listenerName = args[2];
                                    String eventClass = args[3];
                                    String code = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
                                    if(DebugListenerManager.listenerExists(listenerName)) {
                                        sender.sendMessage(String.format(
                                                "§8[§b§lOrion§8]§c Listener with name '§l%s§c' is already present!",
                                                listenerName
                                        ));
                                    } else {
                                        Class<? extends Event> eventClazz;
                                        try {
                                            eventClazz = Ensure.notNull(ClassCache.getEventClasses()
                                                    .get(eventClass), "").asSubclass(Event.class);
                                        } catch (NullPointerException e){
                                            sender.sendMessage(String.format(
                                                    "§8[§b§lOrion§8]§c No such class '§l%s§c'!",
                                                    eventClass
                                            ));
                                            break;
                                        } catch (ClassCastException e){
                                            sender.sendMessage(String.format(
                                                    "§8[§b§lOrion§8]§c Class '§l%s§c' is not Event class!",
                                                    eventClass
                                            ));
                                            break;
                                        }
                                        try {
                                            DebugListenerManager.register(listenerName, eventClazz, code);
                                            sender.sendMessage(String.format(
                                                    "§8[§b§lOrion§8]§7 Listener '§c%s§7' added",
                                                    listenerName
                                            ));
                                        } catch (CompilationFailedException e){
                                            sender.sendMessage(String.format(
                                                    "§8[§b§lOrion§8]§c Failed to compile Groovy script for listener '§l%s§c'. Pasting error...",
                                                    listenerName
                                            ));
                                            StringWriter sw = new StringWriter();
                                            PrintWriter pw = new PrintWriter(sw);
                                            e.printStackTrace(pw);
                                            PasteUtility.pasteText(sw.toString(), url -> {
                                                if(url != null) {
                                                    sender.sendMessage("§8[§b§lOrion§8] §7Stack trace: §n" + url.toString());
                                                } else {
                                                    sender.sendMessage("§8[§b§lOrion§8] §cFailed to paste! See console for more information.");
                                                }
                                            });
                                        }
                                    }
                                } else {
                                    sender.sendMessage("§8[§b§lOrion§8] §cInvalid usage. See §7'§c/orion debug§7'");
                                }
                                break;
                            case "removelistener":
                                if(args.length == 3) {
                                    String listenerName = args[2];
                                    if(DebugListenerManager.listenerExists(listenerName)) {
                                        DebugListenerManager.unregister(listenerName);
                                        sender.sendMessage(String.format(
                                                "§8[§b§lOrion§8]§7 Listener '§c%s§7' unregistered",
                                                listenerName
                                        ));
                                    } else {
                                        sender.sendMessage(String.format(
                                                "§8[§b§lOrion§8]§c Invalid listener §7'§c%s§7'",
                                                listenerName
                                        ));
                                    }
                                } else {
                                    sender.sendMessage("§8[§b§lOrion§8]§7 §cInvalid usage. See §7'§c/orion debug§7'");
                                }
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
        if(args.length > 1) {
            switch (args[0]) {
                case "debug":
                    if(args.length >= 2) {
                        if(args.length >= 3) {
                            switch (args[1]){
                                case "addlistener":
                                    if(args.length == 3){
                                        break;
                                    } else if(args.length == 4) {
                                        return completeList(sender, args, new ArrayList<>(ClassCache.getEventClasses().keySet()));
                                    }
                                    break;
                                case "removelistener":
                                    if(args.length == 3)
                                        return completeList(sender, args, new ArrayList<>(DebugListenerManager.getListeners().keySet()));
                            }
                        } else {
                            return completeList(sender, args, Arrays.asList("info", "addlistener", "removelistener"));
                        }
                    }
                    break;
            }
        } else {
            return completeList(sender, args, Arrays.asList("debug", "reload"));
        }
        return ImmutableList.of();
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
                String.format("§7Java version: §c%s §7(§c%s§7)",
                        javaVersion,
                        System.getProperties().getProperty("java.runtime.version")),
                "§7Java vendor: §c" + System.getProperties().getProperty("java.vendor"),
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

    private static List<String> completeList(CommandSender sender, String[] args, List<String> completions){
        return StringUtil.copyPartialMatches(args[args.length-1], completions, new ArrayList<>());
    }
}
