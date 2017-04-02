package eu.mikroskeem.orion.api.plugin;

import com.github.zafarkhaja.semver.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Mark Vainomaa
 */
public interface PluginContainer {
    String getName();
    Class<?> getPluginClass();
    Version getVersion();

    List<PluginContainer> getDependencies();
    List<PluginContainer> getSoftDependencies();

    default String getDisplayName() {
        return getName();
    }

    default Logger getLogger() {
        return LoggerFactory.getLogger(getDisplayName());
    }
}
