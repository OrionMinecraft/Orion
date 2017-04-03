package eu.mikroskeem.orion.launcher.mixins;

import com.google.common.collect.ImmutableList;
import eu.mikroskeem.orion.api.plugin.PluginManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.PluginsCommand;
import org.bukkit.plugin.Plugin;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mark Vainomaa
 */
@Mixin(PluginsCommand.class)
public abstract class MixinPluginsCommand extends Command {
    public MixinPluginsCommand(){super(null, null, null, ImmutableList.of()); }

    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!testPermission(sender)) return true;

        sender.sendMessage(getPluginList().split("\n"));
        return true;
    }

    private String getPluginList() {
        StringBuilder pluginList = new StringBuilder();
        PluginManager plm = (PluginManager)Bukkit.getPluginManager();

        List<Plugin> ownPlugins = new ArrayList<>();
        List<Plugin> otherPlugins = new ArrayList<>();
        plm.listPlugins().forEach(plugin -> {
            if(plugin.getClass().getName().startsWith("eu.mikroskeem")){
                ownPlugins.add(plugin);
            } else {
                otherPlugins.add(plugin);
            }
        });

        pluginList.append(String.format("§f=== §6Own plugins §f(§6%s§f) ===\n", ownPlugins.size()));
        pluginList.append(String.join("§f, ", orion$transform(ownPlugins)));
        pluginList.append("\n");
        pluginList.append(String.format("§f== §bPublic plugins §f(§b%s§f) =\n", otherPlugins.size()));
        pluginList.append(String.join("§f, ", orion$transform(otherPlugins)));

        return pluginList.toString();
    }

    private List<String> orion$transform(List<Plugin> plugins){
        return plugins.stream().map(this::orion$getName).collect(Collectors.toList());
    }

    private String orion$getName(Plugin plugin){
        return (plugin.isEnabled()? ChatColor.GREEN:ChatColor.RED) + plugin.getName();
    }
}
