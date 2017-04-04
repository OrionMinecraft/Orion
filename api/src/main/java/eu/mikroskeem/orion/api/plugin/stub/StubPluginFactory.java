package eu.mikroskeem.orion.api.plugin.stub;

import eu.mikroskeem.orion.api.plugin.PluginManager;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Mark Vainomaa
 */
public class StubPluginFactory {
    private final List<AbstractFakePlugin> fakePlugins = new ArrayList<>();
    private final Plugin plugin;
    private final PluginManager pluginManager;

    public StubPluginFactory(Plugin parentPlugin){
        this.plugin = parentPlugin;
        this.pluginManager = (PluginManager)parentPlugin.getServer().getPluginManager();
    }

    public synchronized AbstractFakePlugin newPlugin(String name, String version, String mainClass){
        AbstractFakePlugin fakePlugin =
                new AbstractFakePlugin(plugin, name, version, mainClass, null, null){};
        fakePlugins.add(fakePlugin);
        return fakePlugin;
    }

    public synchronized void addPlugin(AbstractFakePlugin plugin){
        String lookupName = plugin.getName().toLowerCase(Locale.ENGLISH);
        pluginManager.getLookupNames().put(lookupName, plugin);
        pluginManager.listPlugins().add(plugin);
        fakePlugins.add(plugin);
    }

    public synchronized void removePlugin(AbstractFakePlugin plugin){
        if (pluginManager.listPlugins().contains(plugin)) pluginManager.listPlugins().remove(plugin);
        if (pluginManager.getLookupNames().containsKey(plugin.getName())) {
            pluginManager.getLookupNames().remove(plugin.getName().toLowerCase(Locale.ENGLISH));
        }
        if (fakePlugins.contains(plugin)) fakePlugins.remove(plugin);
    }

    /* Note: Not needed, SimplePluginManager handles plugin removal already
    synchronized void removeAllPlugins() {
        fakePlugins.forEach(this::removePlugin);
    }
    */
}
