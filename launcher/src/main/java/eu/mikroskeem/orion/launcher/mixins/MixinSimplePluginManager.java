package eu.mikroskeem.orion.launcher.mixins;

import eu.mikroskeem.orion.api.plugin.PluginManager;
import lombok.SneakyThrows;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mark Vainomaa
 */
@Mixin(SimplePluginManager.class)
public abstract class MixinSimplePluginManager implements PluginManager {
    @Shadow(remap = false) @Final private Map<String, Plugin> lookupNames;
    @Shadow(remap = false) @Final private List<Plugin> plugins;

    @Shadow(remap = false) @Final private SimpleCommandMap commandMap;

    @Override
    public Map<String, Plugin> getLookupNames(){
        return this.lookupNames;
    }

    @Override
    public List<Plugin> listPlugins(){
        return this.plugins;
    }

    @Override
    @SneakyThrows(IOException.class)
    public void unloadPlugin(Plugin plugin){
        /* Disable */
        disablePlugin(plugin);

        /* Remove */
        lookupNames.remove(plugin.getName(), plugin);
        plugins.remove(plugin);

        /* Unregister commands */
        new HashMap<>(commandMap.getKnownCommands()).values().stream()
                .filter(c -> c instanceof PluginCommand)
                .map(c -> (PluginCommand)c)
                .filter(c -> c.getPlugin().equals(plugin))
                .forEach(c -> {
                    c.unregister(commandMap);
                    commandMap.getKnownCommands().remove(c.getName(), c);
                    commandMap.getKnownCommands()
                            .remove(plugin.getName() + ":" + c.getName(), c); // Fallback prefix
        });

        /* Unload classloader */
        ClassLoader classLoader = plugin.getClass().getClassLoader();
        if(classLoader instanceof URLClassLoader) ((URLClassLoader) classLoader).close();
        System.gc();
    }
}
