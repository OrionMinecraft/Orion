package eu.mikroskeem.orion.mod.mixins;

import eu.mikroskeem.orion.OrionServerCore;
import eu.mikroskeem.orion.api.Orion;
import eu.mikroskeem.orion.api.OrionServer;
import eu.mikroskeem.orion.internal.interfaces.ExposedPluginManager;
import eu.mikroskeem.orion.api.server.Configuration;
import eu.mikroskeem.orion.internal.debug.DebugListener;
import eu.mikroskeem.orion.internal.debug.DebugListenerManager;
import eu.mikroskeem.orion.internal.interfaces.ExposedJavaPluginLoader;
import eu.mikroskeem.shuriken.common.SneakyThrow;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
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
@Mixin(value = SimplePluginManager.class, remap = false)
@Slf4j
public abstract class MixinSimplePluginManager implements ExposedPluginManager {
    @Shadow @Final private Map<String, Plugin> lookupNames;
    @Shadow @Final private List<Plugin> plugins;
    @Shadow @Final private SimpleCommandMap commandMap;

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

    @Redirect(method = "fireEvent(Lorg/bukkit/event/Event;)V", at = @At(
            value = "INVOKE",
            target = "Lorg/bukkit/event/Event;getHandlers()Lorg/bukkit/event/HandlerList;"
    ))
    @SuppressWarnings("unchecked")
    public <T extends Event> HandlerList fireEventProxy(Event event){
        Configuration configuration = Orion.getServer().getConfiguration();
        if(configuration.getDebug().isScriptEventHandlerAllowed()) {
            Collection<DebugListener<?>> debugListeners = DebugListenerManager
                    .getListenersForEvent(event.getClass());
            debugListeners.forEach(debugListener -> {
                ((DebugListener<T>) debugListener).execute((T)event);
            });
        }
        return event.getHandlers();
    }

    @Inject(method = "fireEvent(Lorg/bukkit/event/Event;)V", at = @At("HEAD"))
    public void onFireEvent(Event event, CallbackInfo callbackInfo){
        Configuration configuration = Orion.getServer().getConfiguration();
        if(configuration.getDebug().isEventDumpingAllowed()) {
            log.info("{}", event.toString());
        }
    }

    @Redirect(method = "fireEvent(Lorg/bukkit/event/Event;)V", at = @At(
            value = "INVOKE",
            target = "Lorg/bukkit/plugin/RegisteredListener;callEvent(Lorg/bukkit/event/Event;)V"
    ))
    public void callEventProxy(RegisteredListener registeredListener, Event event) {
        OrionServer server = Orion.getServer();
        Configuration configuration = server.getConfiguration();
        try {
            registeredListener.callEvent(event);
        } catch (Throwable e){
            if(configuration.getDebug().isReportingEventExceptionsToSentryAllowed()) {
                ((OrionServerCore) server).getSentryReporter()
                        .reportEventPassException(registeredListener, e, event);
            }
            SneakyThrow.throwException(e);
        }
    }
}
