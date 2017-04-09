package eu.mikroskeem.orion.launcher.mixins;

import eu.mikroskeem.orion.api.Orion;
import eu.mikroskeem.orion.api.plugin.PluginManager;
import eu.mikroskeem.orion.api.server.Configuration;
import eu.mikroskeem.orion.internal.debug.DebugListener;
import eu.mikroskeem.orion.internal.debug.DebugListenerManager;
import eu.mikroskeem.orion.internal.interfaces.ExposedJavaPluginLoader;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.PluginClassLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.*;

/**
 * @author Mark Vainomaa
 */
@Mixin(SimplePluginManager.class)
@Slf4j
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

    @Redirect(method = "fireEvent(Lorg/bukkit/event/Event;)V", remap = false, at = @At(
            value = "INVOKE",
            target = "Lorg/bukkit/event/Event;getHandlers()Lorg/bukkit/event/HandlerList;",
            remap = false
    ))
    @SuppressWarnings("unchecked")
    public <T extends eu.mikroskeem.orion.api.events.Event> HandlerList fireEventProxy(Event event){
        Configuration configuration = Orion.getServer().getConfiguration();
        if(configuration.getDebug().isEventDumpingAllowed()) {
            Collection<DebugListener<?>> debugListeners = DebugListenerManager
                    .getListenersForEvent(((Class<T>) event.getClass()));
            debugListeners.forEach(debugListener -> {
                ((DebugListener<T>) debugListener).execute((T)event);
            });
        }
        return event.getHandlers();
    }

    @Inject(method = "fireEvent(Lorg/bukkit/event/Event;)V", remap = false, at = @At(value = "HEAD", remap = false))
    public void onFireEvent(Event event, CallbackInfo callbackInfo){
        Configuration configuration = Orion.getServer().getConfiguration();
        if(configuration.getDebug().isEventDumpingAllowed()) {
            log.info("{}", event.toString());
        }
    }
}
