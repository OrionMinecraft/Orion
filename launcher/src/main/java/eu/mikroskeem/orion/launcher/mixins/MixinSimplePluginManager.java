package eu.mikroskeem.orion.launcher.mixins;

import eu.mikroskeem.orion.api.plugin.PluginManager;
import eu.mikroskeem.orion.internal.interfaces.ExposedJavaPluginLoader;
import lombok.SneakyThrows;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.PluginClassLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
        String pluginName = plugin.getName().toLowerCase(Locale.ENGLISH);

        /* Disable */
        disablePlugin(plugin);

        /* Remove from lists */
        lookupNames.remove(pluginName, plugin);
        plugins.remove(plugin);

        /* Unregister commands */
        new HashMap<>(commandMap.getKnownCommands()).values().stream()
                .filter(c -> c instanceof PluginCommand)
                .map(c -> (PluginCommand)c)
                .filter(c -> c.getPlugin().getName().toLowerCase(Locale.ENGLISH).equals(pluginName))
                .forEach(c -> {
                    c.unregister(commandMap);
                    commandMap.getKnownCommands().remove(c.getName(), c);
                    commandMap.getKnownCommands().remove(pluginName + ":" + c.getName());
        });

        /* Unregister listeners for sure */
        HandlerList.unregisterAll(plugin);

        /* Close classloader and trigger garbage collector */
        ClassLoader classLoader = plugin.getClass().getClassLoader();
        if(plugin.getPluginLoader() instanceof JavaPluginLoader) {
            ExposedJavaPluginLoader jpl = (ExposedJavaPluginLoader)plugin.getPluginLoader();
            PluginClassLoader pcl = (PluginClassLoader)classLoader;
            if(jpl.getLoaders().contains(pcl))
                jpl.getLoaders().remove(pcl);
        }
        if (classLoader instanceof URLClassLoader) ((URLClassLoader) classLoader).close();
        System.gc();
    }
}
