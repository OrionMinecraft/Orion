package eu.mikroskeem.orion.api.plugin;

import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;

/**
 * @author Mark Vainomaa
 */
public interface PluginManager extends org.bukkit.plugin.PluginManager {
    Map<String, Plugin> getLookupNames();
    List<Plugin> listPlugins();
    void unloadPlugin(Plugin plugin);
}
