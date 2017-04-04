package eu.mikroskeem.orion.internal.interfaces;

import java.util.Map;

/**
 * @author Mark Vainomaa
 */
public interface ExposedPluginClassLoader {
    Map<String, Class<?>> getClassesMap();
}
