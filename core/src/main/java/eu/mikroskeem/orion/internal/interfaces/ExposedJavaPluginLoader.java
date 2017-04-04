package eu.mikroskeem.orion.internal.interfaces;

import org.bukkit.plugin.java.PluginClassLoader;

import java.util.List;

/**
 * @author Mark Vainomaa
 */
public interface ExposedJavaPluginLoader {
    List<PluginClassLoader> getLoaders();
}
