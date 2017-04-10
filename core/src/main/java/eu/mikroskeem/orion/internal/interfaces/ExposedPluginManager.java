package eu.mikroskeem.orion.internal.interfaces;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.List;
import java.util.Map;

/**
 * @author Mark Vainomaa
 */
public interface ExposedPluginManager extends PluginManager {
    Map<String, Plugin> getLookupNames();
    List<Plugin> listPlugins();
    void unloadPlugin(Plugin plugin);
}
